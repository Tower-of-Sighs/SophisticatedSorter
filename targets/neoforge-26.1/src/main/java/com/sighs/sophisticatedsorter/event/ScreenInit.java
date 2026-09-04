package com.sighs.sophisticatedsorter.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.sophisticatedsorter.Config;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.common.ContainerScreenBehavior;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.PlatformClient;
import com.sighs.sophisticatedsorter.utils.SearchBoxPositionAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
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
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** Adds Core's controls and search behavior to ordinary 26.1 container screens. */
@EventBusSubscriber(modid = SophisticatedSorter.MODID, value = Dist.CLIENT)
public final class ScreenInit {
    private static final Map<AbstractContainerScreen<?>, State> STATES =
            Collections.synchronizedMap(new WeakHashMap<>());
    /**
     * Settings gear (12x12) definition for the optional fourth button of the top-right group,
     * matching the shared sorter controls of the other targets.
     */
    private static final ButtonDefinition SETTINGS_DEFINITION = new ButtonDefinition(
            Dimension.SQUARE_12,
            GuiHelper.SMALL_BUTTON_BACKGROUND,
            GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND,
            new TextureBlitData(GuiHelper.ICONS, new Position(1, 1), Dimension.SQUARE_256, new UV(19, 99), new Dimension(10, 10)),
            Component.translatable("gui.sophisticatedsorter.settings.open"));

    private static Component dragTooltipHint() {
        String disableKey = disableKeyName();
        return Component.translatable("gui.sophisticatedsorter.drag_hint", disableKey)
                .withStyle(ChatFormatting.GRAY);
    }

    private static String disableKeyName() {
        InputConstants.Key key = ModKeybindings.DISABLE_KEY.getKey();
        if (key.getType() == InputConstants.Type.SCANCODE) {
            String name = GLFW.glfwGetKeyName(-1, key.getValue());
            if (name == null) {
                name = GLFW.glfwGetKeyName(key.getValue(), 0);
            }
            if (name != null) {
                return name.toUpperCase(Locale.ROOT);
            }
        }
        return ModKeybindings.DISABLE_KEY.getTranslatedKeyMessage().getString();
    }

    static {
        ClientUtils.installPlatform(PlatformClient.INSTANCE);
    }

    private ScreenInit() {
    }

    public static List<Component> appendTooltipHint(List<Component> tooltip) {
        List<Component> result = new ArrayList<>(tooltip == null ? List.of() : tooltip);
        result.add(dragTooltipHint());
        return List.copyOf(result);
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
        State state = new State(screen);
        if (!state.behavior.initialize(false, ClientUtils.isValidScreen(), inventoryScreen, false)) {
            return;
        }
        STATES.put(screen, state);

        boolean settingsPresent = ClientUtils.hasContainerSettings();
        int settingsShrink = settingsPresent ? 12 : 0;
        int left = screen.getLeftPos();
        int top = screen.getTopPos();
        int width = screen.getImageWidth();
        ToggleButton<?> toggleButton = new HintToggleButton<>(
                new Position(left + width - 19 - settingsShrink + state.sortGroupX, top + 4 + state.sortGroupY),
                ButtonDefinitions.SORT_BY,
                button -> {
                    if (button == 0) {
                        ClientUtils.toggleSortBy();
                    }
                }, ClientUtils::getSortBy);
        Button sortButton = new HintButton(new Position(
                left + width - 31 - settingsShrink + state.sortGroupX, top + 4 + state.sortGroupY),
                ButtonDefinitions.SORT, button -> {
                    if (button == 0) {
                        ClientUtils.serverSort();
                    }
                });
        state.toggleButton = toggleButton;
        state.sortButton = sortButton;
        if (settingsPresent) {
            Button settingsButton = new HintButton(new Position(
                    left + width - 19 + state.sortGroupX, top + 4 + state.sortGroupY),
                    SETTINGS_DEFINITION, button -> {
                        if (button == 0) {
                            ClientUtils.openSettingsRequested(inventoryScreen);
                        }
                    });
            state.settingsButton = settingsButton;
            event.addListener(settingsButton);
        }

        if (!inventoryScreen) {
            SortButtonsPosition position =
                    net.p3pp3rf1y.sophisticatedcore.Config.CLIENT.sortButtonsPosition.get();
            int searchWidth = (position == SortButtonsPosition.TITLE_LINE_RIGHT ? width - 39 : width - 7) - settingsShrink;
            TextBox searchBox = ClientUtils.createSearchBox(
                    new Position(left + 7 + state.sortGroupX, top + 5 + state.sortGroupY),
                    new Dimension(searchWidth, 10));
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
                    new Position(left + inventoryRight + 5 + state.transferGroupX,
                            top + inventoryTop - 13 + state.transferGroupY),
                    filter -> ClientUtils.serverTransfer(false, filter),
                    ButtonDefinitions.TRANSFER_TO_INVENTORY,
                    ButtonDefinitions.TRANSFER_TO_INVENTORY_FILTERED);
            Button transferToStorageButton = new ShiftTransferButton(
                    new Position(left + inventoryRight - 7 + state.transferGroupX,
                            top + inventoryTop - 13 + state.transferGroupY),
                    filter -> ClientUtils.serverTransfer(true, filter),
                    ButtonDefinitions.TRANSFER_TO_STORAGE,
                    ButtonDefinitions.TRANSFER_TO_STORAGE_FILTERED);
            state.transferToInventoryButton = transferToInventoryButton;
            state.transferToStorageButton = transferToStorageButton;
            event.addListener(transferToInventoryButton);
            event.addListener(transferToStorageButton);
        }
    }

    @SubscribeEvent
    public static void handleRightMousePress(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 1 || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        State state = STATES.get(screen);
        if (state != null && state.beginDrag(event.getMouseX(), event.getMouseY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void handleRightMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        if (event.getMouseButton() != 1 || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        State state = STATES.get(screen);
        if (state != null && state.isDragging()) {
            state.drag(screen, event.getDragX(), event.getDragY());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void handleRightMouseRelease(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() != 1 || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        State state = STATES.get(screen);
        if (state != null && state.isDragging()) {
            state.endDrag();
            event.setCanceled(true);
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
        if (state.settingsButton != null) {
            state.settingsButton.extractTooltip(owner, graphics, mouseX, mouseY);
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
        private final String screenType;
        private int sortGroupX;
        private int sortGroupY;
        private int transferGroupX;
        private int transferGroupY;
        private TextBox searchBox;
        private ToggleButton<?> toggleButton;
        private Button sortButton;
        private Button settingsButton;
        private Button transferToInventoryButton;
        private Button transferToStorageButton;
        private ButtonGroup draggingGroup;
        private int dragStartX;
        private int dragStartY;
        private double dragTotalX;
        private double dragTotalY;

        private State(AbstractContainerScreen<?> screen) {
            this.inventoryScreen = screen instanceof InventoryScreen;
            this.screenType = screen.getClass().getName();
            Config.ButtonPositions positions = Config.getButtonPositions(screenType);
            this.sortGroupX = positions.sortX();
            this.sortGroupY = positions.sortY();
            this.transferGroupX = positions.transferX();
            this.transferGroupY = positions.transferY();
        }

        private boolean beginDrag(double mouseX, double mouseY) {
            if (isOverSortGroup(mouseX, mouseY)) {
                draggingGroup = ButtonGroup.SORT;
            } else if (isOverTransferGroup(mouseX, mouseY)) {
                draggingGroup = ButtonGroup.TRANSFER;
            } else {
                return false;
            }
            dragStartX = draggingGroup == ButtonGroup.SORT ? sortGroupX : transferGroupX;
            dragStartY = draggingGroup == ButtonGroup.SORT ? sortGroupY : transferGroupY;
            dragTotalX = 0;
            dragTotalY = 0;
            return true;
        }

        private boolean isDragging() {
            return draggingGroup != null;
        }

        private void drag(AbstractContainerScreen<?> screen, double deltaX, double deltaY) {
            if (draggingGroup == null) {
                return;
            }
            dragTotalX += deltaX;
            dragTotalY += deltaY;
            int nextX = dragStartX + (int) Math.round(dragTotalX);
            int nextY = dragStartY + (int) Math.round(dragTotalY);
            if (draggingGroup == ButtonGroup.SORT) {
                sortGroupX = nextX;
                sortGroupY = nextY;
            } else {
                transferGroupX = nextX;
                transferGroupY = nextY;
            }
            reposition(screen, true);
        }

        private void endDrag() {
            if (draggingGroup == null) {
                return;
            }
            Config.saveButtonPositions(screenType, sortGroupX, sortGroupY, transferGroupX, transferGroupY);
            draggingGroup = null;
        }

        private boolean isOverSortGroup(double mouseX, double mouseY) {
            return (searchBox != null && searchBox.isMouseOver(mouseX, mouseY))
                    || (sortButton != null && sortButton.isMouseOver(mouseX, mouseY))
                    || (toggleButton != null && toggleButton.isMouseOver(mouseX, mouseY))
                    || (settingsButton != null && settingsButton.isMouseOver(mouseX, mouseY));
        }

        private boolean isOverTransferGroup(double mouseX, double mouseY) {
            return (transferToInventoryButton != null && transferToInventoryButton.isMouseOver(mouseX, mouseY))
                    || (transferToStorageButton != null && transferToStorageButton.isMouseOver(mouseX, mouseY));
        }

        private void reposition(AbstractContainerScreen<?> screen) {
            reposition(screen, false);
        }

        private void reposition(AbstractContainerScreen<?> screen, boolean forceSearchPosition) {
            if (sortButton == null) {
                return;
            }
            int settingsShrink = settingsButton != null ? 12 : 0;
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
                        left + 8 + 149 - settingsShrink + sortGroupX,
                        top + 84 - 2 + sortGroupY));
                sortButton.setPosition(new Position(
                        left + 8 + 137 - settingsShrink + sortGroupX,
                        top + 84 - 2 + sortGroupY));
                if (settingsButton != null) {
                    settingsButton.setPosition(new Position(
                            left + 8 + 149 + sortGroupX,
                            top + 84 - 2 + sortGroupY));
                }
            } else {
                toggleButton.setPosition(new Position(
                        left + width - 19 - settingsShrink + sortGroupX, top + 4 + sortGroupY));
                sortButton.setPosition(new Position(
                        left + width - 31 - settingsShrink + sortGroupX, top + 4 + sortGroupY));
                if (settingsButton != null) {
                    settingsButton.setPosition(new Position(
                            left + width - 19 + sortGroupX, top + 4 + sortGroupY));
                }
                if (searchBox != null
                        && (forceSearchPosition || searchBox.isFocused() || !searchBox.getValue().isEmpty())) {
                    SortButtonsPosition position =
                            net.p3pp3rf1y.sophisticatedcore.Config.CLIENT.sortButtonsPosition.get();
                    int searchWidth = (position == SortButtonsPosition.TITLE_LINE_RIGHT ? width - 39 : width - 7) - settingsShrink;
                    int searchX = left + 7 + sortGroupX;
                    if (searchBox instanceof SearchBoxPositionAccess access) {
                        access.sophisticatedSorter$setMaximizedPosition(searchX, searchWidth);
                    }
                    boolean collapsed = !searchBox.isFocused()
                            && searchBox.getValue().isEmpty()
                            && searchBox.getWidth() <= searchBox.getHeight();
                    int visibleSearchX = collapsed
                            ? searchX + searchWidth - searchBox.getHeight()
                            : searchX;
                    searchBox.setPosition(new Position(visibleSearchX, top + 5 + sortGroupY));
                } else if (searchBox instanceof SearchBoxPositionAccess access) {
                    SortButtonsPosition position =
                            net.p3pp3rf1y.sophisticatedcore.Config.CLIENT.sortButtonsPosition.get();
                    int searchWidth = (position == SortButtonsPosition.TITLE_LINE_RIGHT ? width - 39 : width - 7) - settingsShrink;
                    access.sophisticatedSorter$setMaximizedPosition(left + 7 + sortGroupX, searchWidth);
                }
                transferToInventoryButton.setPosition(new Position(
                        left + inventoryRight + 5 + transferGroupX,
                        top + inventoryTop - 13 + transferGroupY));
                transferToStorageButton.setPosition(new Position(
                        left + inventoryRight - 7 + transferGroupX,
                        top + inventoryTop - 13 + transferGroupY));
            }
        }
    }

    private enum ButtonGroup {
        SORT,
        TRANSFER
    }

    private static final class HintButton extends Button {
        private HintButton(Position position, ButtonDefinition definition, IntConsumer onClick) {
            super(position, definition, onClick);
        }

        @Override
        protected List<Component> getTooltip() {
            return appendTooltipHint(super.getTooltip());
        }
    }

    private static final class HintToggleButton<T extends Comparable<T>> extends ToggleButton<T> {
        private HintToggleButton(Position position, ButtonDefinition.Toggle<T> definition,
                                 IntConsumer onClick, Supplier<T> getState) {
            super(position, definition, onClick, getState);
        }

        @Override
        protected List<Component> getTooltip(StateData stateData) {
            return appendTooltipHint(super.getTooltip(stateData));
        }
    }
}