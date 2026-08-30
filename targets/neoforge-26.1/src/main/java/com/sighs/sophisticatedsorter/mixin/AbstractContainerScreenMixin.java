package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.event.ScreenInit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 26.1 GUI bridge for search filtering and Core-style widget tooltips. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "extractSlot", at = @At("RETURN"))
    private void sophisticatedSorter$overlayFilteredSlot(
            GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (!(screen instanceof CreativeModeInventoryScreen)
                && ScreenInit.isFiltered(screen, slot.getItem())) {
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x90202020);
        }
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"))
    private void sophisticatedSorter$renderWidgetTooltips(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (screen instanceof CreativeModeInventoryScreen) {
            return;
        }
        ScreenInit.renderTooltips(screen, graphics, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void sophisticatedSorter$clearSearchFocus(
            net.minecraft.client.input.MouseButtonEvent event, boolean isKeyboardClick,
            CallbackInfoReturnable<Boolean> cir) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (screen instanceof CreativeModeInventoryScreen || screen instanceof StorageScreenBase<?>) {
            return;
        }
        GuiEventListener focused = ((Screen) (Object) this).getFocused();
        if (focused instanceof TextBox textBox && !textBox.isMouseOver(event.x(), event.y())) {
            textBox.setFocused(false);
            ((Screen) (Object) this).setFocused(null);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sophisticatedSorter$tick(CallbackInfo ci) {
        ScreenInit.tick((AbstractContainerScreen<?>) (Object) this);
    }
}
