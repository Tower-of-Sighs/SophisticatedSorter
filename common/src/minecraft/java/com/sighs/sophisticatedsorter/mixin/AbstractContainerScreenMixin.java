package com.sighs.sophisticatedsorter.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sighs.sophisticatedsorter.common.ButtonPositions;
import com.sighs.sophisticatedsorter.common.ContainerScreenBehavior;
import com.sighs.sophisticatedsorter.common.ContainerScreenLayout;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.HintButton;
import com.sighs.sophisticatedsorter.utils.HintToggleButton;
import com.sighs.sophisticatedsorter.utils.SearchBoxPositionAccess;
import com.sighs.sophisticatedsorter.utils.ShiftTransferButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SortButtonsPosition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(value = AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Shadow protected int leftPos;
    @Shadow protected int imageWidth;
    @Shadow protected int topPos;
    @Shadow protected int inventoryLabelX;
    @Shadow protected int inventoryLabelY;
    @Shadow protected abstract void init();

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Unique private final ContainerScreenBehavior<ItemStack> behavior = new ContainerScreenBehavior<>();
    /** Settings gear (12x12) definition for the optional fourth button of the top-right group. */
    @Unique private static final net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition SETTINGS_DEFINITION =
            new net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition(
                    net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension.SQUARE_12,
                    net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.SMALL_BUTTON_BACKGROUND,
                    net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND,
                    new net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData(
                            net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.ICONS,
                            new net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position(1, 1),
                            net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension.SQUARE_256,
                            new net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV(19, 99),
                            new net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension(10, 10)),
                    Component.translatable("gui.sophisticatedsorter.settings.open"));
    @Unique private TextBox searchBox;
    @Unique private ToggleButton toggleButton;
    @Unique private Button sortButton;
    @Unique private Button settingsButton;
    @Unique private Button transferToInventoryButton;
    @Unique private Button transferToStorageButton;
    @Unique private int sortGroupX;
    @Unique private int sortGroupY;
    @Unique private int transferGroupX;
    @Unique private int transferGroupY;
    @Unique private boolean draggingSortGroup;
    @Unique private boolean draggingTransferGroup;
    @Unique private int dragStartX;
    @Unique private int dragStartY;
    @Unique private double dragTotalX;
    @Unique private double dragTotalY;

    @Inject(method = "init", at = @At("RETURN"))
    private void initializeSorterControls(CallbackInfo ci) {
        boolean validScreen = ClientUtils.isValidScreen();
        boolean inventoryScreen = Minecraft.getInstance().screen instanceof InventoryScreen;
        if (!behavior.initialize((Object) this instanceof CreativeModeInventoryScreen, validScreen,
                inventoryScreen, (Object) this instanceof StorageScreenBase)) {
            return;
        }

        var menu = Minecraft.getInstance().player.containerMenu;
        Position blankPosition = new Position(0, 0);

        String screenType = ClientUtils.getScreenId(this);
        ButtonPositions buttonPositions = ClientUtils.getButtonPositions(screenType);
        this.sortGroupX = buttonPositions.sortX();
        this.sortGroupY = buttonPositions.sortY();
        this.transferGroupX = buttonPositions.transferX();
        this.transferGroupY = buttonPositions.transferY();

        SortButtonsPosition sortButtonsPosition = Config.CLIENT.sortButtonsPosition.get();
        if (sortButtonsPosition != SortButtonsPosition.HIDDEN) {
            toggleButton = new HintToggleButton(blankPosition, ButtonDefinitions.SORT_BY, button -> {
                if (button == 0) {
                    ClientUtils.toggleSortBy();
                }
            }, ClientUtils::getSortBy);
            addRenderableWidget(toggleButton);

            sortButton = new HintButton(blankPosition, ButtonDefinitions.SORT, button -> {
                if (button == 0) {
                    ClientUtils.serverSort();
                }
            });
            addRenderableWidget(sortButton);

            if (ClientUtils.hasContainerSettings()) {
                boolean playerInventoryScreen = inventoryScreen;
                settingsButton = new HintButton(blankPosition, SETTINGS_DEFINITION, button -> {
                    if (button == 0) {
                        ClientUtils.openSettingsRequested(playerInventoryScreen);
                    }
                });
                addRenderableWidget(settingsButton);
            }

            if (!behavior.isInventoryScreen()) {
                int settingsShrink = settingsButton != null ? 12 : 0;
                int xEnd = sortButtonsPosition == SortButtonsPosition.TITLE_LINE_RIGHT
                        ? new Position(leftPos + imageWidth - 31 - settingsShrink, topPos + 4).x() - 1 - leftPos
                        : imageWidth - 7 - settingsShrink;
                int width = xEnd - 7;
                Position searchPosition = new Position(
                        leftPos + 7 + sortGroupX, topPos + 5 + sortGroupY);
                searchBox = ClientUtils.createSearchBox(searchPosition, new Dimension(width, 10), null);
                addRenderableWidget(searchBox);

                Consumer<Boolean> transferToInventory = filterByContents -> ClientUtils.serverTransfer(false, filterByContents);
                transferToInventoryButton = new ShiftTransferButton(
                        blankPosition, transferToInventory,
                        ButtonDefinitions.TRANSFER_TO_INVENTORY,
                        ButtonDefinitions.TRANSFER_TO_INVENTORY_FILTERED);
                addRenderableWidget(transferToInventoryButton);

                Consumer<Boolean> transferToStorage = filterByContents -> ClientUtils.serverTransfer(true, filterByContents);
                transferToStorageButton = new ShiftTransferButton(
                        blankPosition, transferToStorage,
                        ButtonDefinitions.TRANSFER_TO_STORAGE,
                        ButtonDefinitions.TRANSFER_TO_STORAGE_FILTERED);
                addRenderableWidget(transferToStorageButton);
            }
        }

        resetWidgetPosition(menu);
    }

    @Unique
    private void resetWidgetPosition(AbstractContainerMenu menu) {
        resetWidgetPosition(menu, false);
    }

    @Unique
    private void resetWidgetPosition(AbstractContainerMenu menu, boolean forceSearchPosition) {
        if (sortButton == null) {
            return;
        }

        int inventoryRight = 0;
        int inventoryTop = 0;
        if (!behavior.isInventoryScreen()) {
            for (Slot slot : menu.slots) {
                if (slot.container instanceof Inventory) {
                    inventoryRight = behavior.maxInventoryX(inventoryRight, slot.x);
                    inventoryTop = behavior.minInventoryY(inventoryTop, slot.y);
                }
            }
        }

        ContainerScreenLayout.Positions positions = behavior.positions(
                leftPos, topPos, imageWidth, inventoryLabelX, inventoryLabelY,
                inventoryRight, inventoryTop);
        boolean shiftForSettings = settingsButton != null;
        Position topPosition1 = new Position(
                positions.topToggleX() + sortGroupX + (shiftForSettings ? -12 : 0),
                positions.topToggleY() + sortGroupY);
        Position topPosition2 = new Position(
                positions.topSortX() + sortGroupX + (shiftForSettings ? -12 : 0),
                positions.topSortY() + sortGroupY);
        Position bottomPosition1 = new Position(
                positions.bottomToggleX() + transferGroupX, positions.bottomToggleY() + transferGroupY);
        Position bottomPosition2 = new Position(
                positions.bottomSortX() + transferGroupX, positions.bottomSortY() + transferGroupY);

        if (behavior.isInventoryScreen()) {
            toggleButton.setPosition(new Position(
                    positions.bottomToggleX() + sortGroupX + (shiftForSettings ? -12 : 0),
                    positions.bottomToggleY() + sortGroupY));
            sortButton.setPosition(new Position(
                    positions.bottomSortX() + sortGroupX + (shiftForSettings ? -12 : 0),
                    positions.bottomSortY() + sortGroupY));
            if (settingsButton != null) {
                settingsButton.setPosition(new Position(
                        positions.bottomToggleX() + sortGroupX, positions.bottomToggleY() + sortGroupY));
            }
        } else {
            toggleButton.setPosition(topPosition1);
            sortButton.setPosition(topPosition2);
            if (settingsButton != null) {
                settingsButton.setPosition(new Position(
                        positions.topToggleX() + sortGroupX, positions.topToggleY() + sortGroupY));
            }
        }
        if (transferToInventoryButton != null) {
            transferToInventoryButton.setPosition(bottomPosition1);
            transferToStorageButton.setPosition(bottomPosition2);
        }
        if (searchBox != null && !behavior.isInventoryScreen()) {
            SortButtonsPosition sortButtonsPosition = Config.CLIENT.sortButtonsPosition.get();
            int settingsShrink = shiftForSettings ? 12 : 0;
            int searchWidth = (sortButtonsPosition == SortButtonsPosition.TITLE_LINE_RIGHT
                    ? imageWidth - 39
                    : imageWidth - 14) - settingsShrink;
            int searchX = leftPos + 7 + sortGroupX;
            if (searchBox instanceof SearchBoxPositionAccess access) {
                access.sophisticatedSorter$setMaximizedPosition(searchX, searchWidth);
            }
            if (forceSearchPosition || searchBox.isFocused() || !searchBox.getValue().isEmpty()) {
                boolean collapsed = !searchBox.isFocused()
                        && searchBox.getValue().isEmpty()
                        && searchBox.getWidth() <= searchBox.getHeight();
                int visibleSearchX = collapsed
                        ? searchX + searchWidth - searchBox.getHeight()
                        : searchX;
                searchBox.setPosition(new Position(visibleSearchX, topPos + 5 + sortGroupY));
            }
        }
    }

    @Unique
    private boolean isOverSortGroup(double mouseX, double mouseY) {
        return (searchBox != null && searchBox.isMouseOver(mouseX, mouseY))
                || (sortButton != null && sortButton.isMouseOver(mouseX, mouseY))
                || (toggleButton != null && toggleButton.isMouseOver(mouseX, mouseY))
                || (settingsButton != null && settingsButton.isMouseOver(mouseX, mouseY));
    }

    @Unique
    private boolean isOverTransferGroup(double mouseX, double mouseY) {
        return (transferToInventoryButton != null && transferToInventoryButton.isMouseOver(mouseX, mouseY))
                || (transferToStorageButton != null && transferToStorageButton.isMouseOver(mouseX, mouseY));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void beginGroupDrag(double mouseX, double mouseY, int button,
                                CallbackInfoReturnable<Boolean> cir) {
        if (button != 1 || !behavior.shouldHandleClick((Object) this instanceof CreativeModeInventoryScreen,
                (Object) this instanceof StorageScreenBase<?>)) {
            return;
        }
        if (isOverSortGroup(mouseX, mouseY)) {
            draggingSortGroup = true;
        } else if (isOverTransferGroup(mouseX, mouseY)) {
            draggingTransferGroup = true;
        } else {
            return;
        }
        dragStartX = draggingSortGroup ? sortGroupX : transferGroupX;
        dragStartY = draggingSortGroup ? sortGroupY : transferGroupY;
        dragTotalX = 0;
        dragTotalY = 0;
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void dragGroup(double mouseX, double mouseY, int button, double dragX, double dragY,
                           CallbackInfoReturnable<Boolean> cir) {
        if (!draggingSortGroup && !draggingTransferGroup) {
            return;
        }
        dragTotalX += dragX;
        dragTotalY += dragY;
        int nextX = dragStartX + (int) Math.round(dragTotalX);
        int nextY = dragStartY + (int) Math.round(dragTotalY);
        if (draggingSortGroup) {
            sortGroupX = nextX;
            sortGroupY = nextY;
        } else {
            transferGroupX = nextX;
            transferGroupY = nextY;
        }
        var menu = Minecraft.getInstance().player.containerMenu;
        resetWidgetPosition(menu, true);
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void endGroupDrag(double mouseX, double mouseY, int button,
                              CallbackInfoReturnable<Boolean> cir) {
        if (!draggingSortGroup && !draggingTransferGroup) {
            return;
        }
        String screenType = ClientUtils.getScreenId(this);
        ClientUtils.saveButtonPositions(screenType,
                new ButtonPositions(sortGroupX, sortGroupY, transferGroupX, transferGroupY));
        draggingSortGroup = false;
        draggingTransferGroup = false;
        cir.setReturnValue(true);
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void renderSorterTooltips(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        if (!behavior.shouldHandleTooltip((Object) this instanceof CreativeModeInventoryScreen)) {
            return;
        }
        for (Renderable renderable : renderables) {
            if (renderable instanceof Button button) {
                button.renderTooltip(this, guiGraphics, x, y);
            }
            if (renderable instanceof TextBox textBox) {
                textBox.renderTooltip(this, guiGraphics, x, y);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void clearSearchFocus(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!behavior.shouldHandleClick((Object) this instanceof CreativeModeInventoryScreen,
                (Object) this instanceof StorageScreenBase<?>)) {
            return;
        }
        if (getFocused() instanceof TextBox && !getFocused().isMouseOver(x, y)) {
            getFocused().setFocused(false);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateSorterSearch(CallbackInfo ci) {
        if (!behavior.shouldHandleTick((Object) this instanceof CreativeModeInventoryScreen)) {
            return;
        }
        if (searchBox != null) {
            behavior.updateSearch(searchBox.getValue(), ClientUtils::getStackFilter);
        }
        if (behavior.shouldReinitialize(ClientUtils.isValidScreen())) {
            renderables.clear();
            init();
        }
        var menu = Minecraft.getInstance().player.containerMenu;
        resetWidgetPosition(menu);
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void dimFilteredSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (behavior.shouldHandleSlotRender((Object) this instanceof CreativeModeInventoryScreen)
                && behavior.isFiltered(slot.getItem())) {
            RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1.0f);
        }
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void overlayFilteredSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (behavior.shouldHandleSlotRender((Object) this instanceof CreativeModeInventoryScreen)
                && behavior.isFiltered(slot.getItem())) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            int x = slot.x;
            int y = slot.y;
            guiGraphics.fill(x, y, x + 16, y + 16, 0x90202020);
        }
    }
}
