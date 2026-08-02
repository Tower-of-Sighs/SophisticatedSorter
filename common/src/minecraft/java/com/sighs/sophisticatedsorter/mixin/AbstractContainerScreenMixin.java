package com.sighs.sophisticatedsorter.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sighs.sophisticatedsorter.common.ContainerScreenBehavior;
import com.sighs.sophisticatedsorter.common.ContainerScreenLayout;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.visual.VisualStorageScreen;
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
    @Unique private TextBox searchBox;
    @Unique private ToggleButton toggleButton;
    @Unique private Button sortButton;
    @Unique private Button transferToInventoryButton;
    @Unique private Button transferToStorageButton;

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
        Position searchPosition = new Position(leftPos + 7, topPos + 5);

        SortButtonsPosition sortButtonsPosition = Config.CLIENT.sortButtonsPosition.get();
        if (sortButtonsPosition != SortButtonsPosition.HIDDEN) {
            toggleButton = new ToggleButton(blankPosition, ButtonDefinitions.SORT_BY, button -> {
                if (button == 0) {
                    ClientUtils.toggleSortBy();
                }
            }, ClientUtils::getSortBy);
            addRenderableWidget(toggleButton);

            sortButton = new Button(blankPosition, ButtonDefinitions.SORT, button -> {
                if (button == 0) {
                    ClientUtils.serverSort();
                }
            });
            addRenderableWidget(sortButton);

            if (!behavior.isInventoryScreen()) {
                int xEnd = sortButtonsPosition == SortButtonsPosition.TITLE_LINE_RIGHT
                        ? new Position(leftPos + imageWidth - 31, topPos + 4).x() - 1 - leftPos
                        : imageWidth - 7;
                int width = xEnd - 7;
                searchBox = ClientUtils.createSearchBox(searchPosition, new Dimension(width, 10), null);
                addRenderableWidget(searchBox);

                var visualScreen = new VisualStorageScreen();
                Consumer<Boolean> transferToInventory = filterByContents -> ClientUtils.serverTransfer(false, filterByContents);
                transferToInventoryButton = ClientUtils.createTransferButton(
                        visualScreen, transferToInventory,
                        ButtonDefinitions.TRANSFER_TO_INVENTORY,
                        ButtonDefinitions.TRANSFER_TO_INVENTORY_FILTERED);
                addRenderableWidget(transferToInventoryButton);

                Consumer<Boolean> transferToStorage = filterByContents -> ClientUtils.serverTransfer(true, filterByContents);
                transferToStorageButton = ClientUtils.createTransferButton(
                        visualScreen, transferToStorage,
                        ButtonDefinitions.TRANSFER_TO_STORAGE,
                        ButtonDefinitions.TRANSFER_TO_STORAGE_FILTERED);
                addRenderableWidget(transferToStorageButton);
            }
        }

        resetWidgetPosition(menu);
    }

    @Unique
    private void resetWidgetPosition(AbstractContainerMenu menu) {
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
        Position topPosition1 = new Position(positions.topToggleX(), positions.topToggleY());
        Position topPosition2 = new Position(positions.topSortX(), positions.topSortY());
        Position bottomPosition1 = new Position(positions.bottomToggleX(), positions.bottomToggleY());
        Position bottomPosition2 = new Position(positions.bottomSortX(), positions.bottomSortY());

        if (behavior.isInventoryScreen()) {
            toggleButton.setPosition(bottomPosition1);
            sortButton.setPosition(bottomPosition2);
        } else {
            toggleButton.setPosition(topPosition1);
            sortButton.setPosition(topPosition2);
        }
        if (searchBox != null) {
            transferToInventoryButton.setPosition(bottomPosition1);
            transferToStorageButton.setPosition(bottomPosition2);
        }
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
