package com.sighs.sophisticatedsorter.event;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.common.ContainerScreenBehavior;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.PlatformClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SortButtonsPosition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Adds Core's controls and search behavior to ordinary 26.1 container screens. */
@EventBusSubscriber(modid = SophisticatedSorter.MODID, value = Dist.CLIENT)
public final class ScreenInit {
    private static final Map<AbstractContainerScreen<?>, State> STATES =
            Collections.synchronizedMap(new WeakHashMap<>());

    static {
        ClientUtils.installPlatform(PlatformClient.INSTANCE);
    }

    private ScreenInit() {
    }

    @SubscribeEvent
    public static void addSorterControls(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                || screen instanceof CreativeModeInventoryScreen
                || screen instanceof StorageScreenBase<?>
                || !isSortableScreen(screen)
                || STATES.containsKey(screen)
                || net.p3pp3rf1y.sophisticatedcore.Config.CLIENT.sortButtonsPosition.get()
                == SortButtonsPosition.HIDDEN) {
            return;
        }

        boolean inventoryScreen = screen instanceof InventoryScreen;
        State state = new State(inventoryScreen);
        if (!state.behavior.initialize(false, ClientUtils.isValidScreen(), inventoryScreen, false)) {
            return;
        }
        STATES.put(screen, state);

        int left = screen.getLeftPos();
        int top = screen.getTopPos();
        int width = screen.getImageWidth();
        ToggleButton<?> toggleButton = new ToggleButton<>(
                new Position(left + width - 19, top + 4), ButtonDefinitions.SORT_BY,
                button -> {
                    if (button == 0) {
                        ClientUtils.toggleSortBy();
                    }
                }, ClientUtils::getSortBy);
        Button sortButton = new Button(new Position(left + width - 31, top + 4),
                ButtonDefinitions.SORT, button -> {
                    if (button == 0) {
                        ClientUtils.serverSort();
                    }
                });
        state.toggleButton = toggleButton;
        state.sortButton = sortButton;

        if (!inventoryScreen) {
            SortButtonsPosition position =
                    net.p3pp3rf1y.sophisticatedcore.Config.CLIENT.sortButtonsPosition.get();
            int searchWidth = position == SortButtonsPosition.TITLE_LINE_RIGHT ? width - 39 : width - 7;
            TextBox searchBox = ClientUtils.createSearchBox(
                    new Position(left + 7, top + 5), new Dimension(searchWidth, 10));
            if (searchBox != null) {
                searchBox.setResponder(value -> state.behavior.updateSearch(value, ClientUtils::getStackFilter));
                state.searchBox = searchBox;
                event.addListener(searchBox);
            }
        }

        event.addListener(toggleButton);
        event.addListener(sortButton);
        if (!inventoryScreen) {
            int inventoryRight = 0;
            int inventoryTop = 0;
            for (Slot slot : screen.getMenu().slots) {
                if (slot.container instanceof Inventory) {
                    inventoryRight = state.behavior.maxInventoryX(inventoryRight, slot.x);
                    inventoryTop = state.behavior.minInventoryY(inventoryTop, slot.y);
                }
            }
            Button transferToInventoryButton = new ShiftTransferButton(
                    new Position(left + inventoryRight + 5, top + inventoryTop - 13),
                    filter -> ClientUtils.serverTransfer(false, filter),
                    ButtonDefinitions.TRANSFER_TO_INVENTORY,
                    ButtonDefinitions.TRANSFER_TO_INVENTORY_FILTERED);
            Button transferToStorageButton = new ShiftTransferButton(
                    new Position(left + inventoryRight - 7, top + inventoryTop - 13),
                    filter -> ClientUtils.serverTransfer(true, filter),
                    ButtonDefinitions.TRANSFER_TO_STORAGE,
                    ButtonDefinitions.TRANSFER_TO_STORAGE_FILTERED);
            state.transferToInventoryButton = transferToInventoryButton;
            state.transferToStorageButton = transferToStorageButton;
            event.addListener(transferToInventoryButton);
            event.addListener(transferToStorageButton);
        }
    }

    private static boolean isSortableScreen(AbstractContainerScreen<?> screen) {
        return screen instanceof InventoryScreen || ClientUtils.isValidScreen();
    }

    public static boolean isFiltered(AbstractContainerScreen<?> screen, ItemStack stack) {
        State state = STATES.get(screen);
        return state != null && state.behavior.isFiltered(stack);
    }

    public static void renderTooltips(AbstractContainerScreen<?> screen,
                                      GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        State state = STATES.get(screen);
        if (state == null) {
            return;
        }
        Screen owner = screen;
        if (state.toggleButton != null) {
            state.toggleButton.extractTooltip(owner, graphics, mouseX, mouseY);
        }
        if (state.sortButton != null) {
            state.sortButton.extractTooltip(owner, graphics, mouseX, mouseY);
        }
        if (state.searchBox != null) {
            state.searchBox.extractTooltip(owner, graphics, mouseX, mouseY);
        }
        if (state.transferToInventoryButton != null) {
            state.transferToInventoryButton.extractTooltip(owner, graphics, mouseX, mouseY);
        }
        if (state.transferToStorageButton != null) {
            state.transferToStorageButton.extractTooltip(owner, graphics, mouseX, mouseY);
        }
    }

    public static void tick(AbstractContainerScreen<?> screen) {
        State state = STATES.get(screen);
        if (state == null) {
            if (!(screen instanceof CreativeModeInventoryScreen)
                    && !(screen instanceof StorageScreenBase<?>)
                    && net.p3pp3rf1y.sophisticatedcore.Config.CLIENT.sortButtonsPosition.get()
                    != SortButtonsPosition.HIDDEN
                    && isSortableScreen(screen)) {
                screen.init(screen.width, screen.height);
            }
            return;
        }
        boolean validScreen = ClientUtils.isValidScreen();
        if (state.behavior.shouldReinitialize(validScreen)) {
            STATES.remove(screen);
            screen.init(screen.width, screen.height);
            return;
        }
        if (state.searchBox != null) {
            state.behavior.updateSearch(state.searchBox.getValue(), ClientUtils::getStackFilter);
        }
        state.reposition(screen);
    }

    private static final class State {
        private final ContainerScreenBehavior<ItemStack> behavior = new ContainerScreenBehavior<>();
        private final boolean inventoryScreen;
        private TextBox searchBox;
        private ToggleButton<?> toggleButton;
        private Button sortButton;
        private Button transferToInventoryButton;
        private Button transferToStorageButton;

        private State(boolean inventoryScreen) {
            this.inventoryScreen = inventoryScreen;
        }

        private void reposition(AbstractContainerScreen<?> screen) {
            if (sortButton == null) {
                return;
            }
            int inventoryRight = 0;
            int inventoryTop = 0;
            if (!inventoryScreen) {
                for (Slot slot : screen.getMenu().slots) {
                    if (slot.container instanceof Inventory) {
                        inventoryRight = behavior.maxInventoryX(inventoryRight, slot.x);
                        inventoryTop = behavior.minInventoryY(inventoryTop, slot.y);
                    }
                }
            }
            int left = screen.getLeftPos();
            int top = screen.getTopPos();
            int width = screen.getImageWidth();
            if (inventoryScreen) {
                toggleButton.setPosition(new Position(
                        left + 8 + 149,
                        top + 84 - 2));
                sortButton.setPosition(new Position(
                        left + 8 + 137,
                        top + 84 - 2));
            } else {
                toggleButton.setPosition(new Position(left + width - 19, top + 4));
                sortButton.setPosition(new Position(left + width - 31, top + 4));
                // Core anchors the collapsed search icon to the search area's right edge.
                if (searchBox != null
                        && (searchBox.isFocused() || !searchBox.getValue().isEmpty())) {
                    searchBox.setPosition(new Position(left + 7, top + 5));
                }
                transferToInventoryButton.setPosition(new Position(
                        left + inventoryRight + 5, top + inventoryTop - 13));
                transferToStorageButton.setPosition(new Position(
                        left + inventoryRight - 7, top + inventoryTop - 13));
            }
        }
    }
}
