package com.sighs.sophisticatedsorter.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import com.sighs.sophisticatedsorter.utils.SearchBoxPositionAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

/** Lets Core's search box work on ordinary screens where its owner is null. */
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox", remap = false)
public final class SearchBoxMixin implements SearchBoxPositionAccess {
    @Shadow @Final @Mutable private int maximizedX;
    @Shadow @Final @Mutable private int maximizedWidth;

    @Redirect(
            method = "mouseClicked",
            at = @At(value = "INVOKE",
                    target = "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"),
            require = 0)
    private void sophisticatedSorter$redirectFocus(StorageScreenBase<?> owner,
                                                    GuiEventListener listener) {
        if (owner != null) {
            owner.setFocused(listener);
        } else if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.setFocused(listener);
        }
    }

    @Redirect(
            method = "extractTooltip",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"),
            require = 0)
    private void sophisticatedSorter$appendTooltipHint(
            GuiGraphicsExtractor graphics, Font font, List<Component> tooltip,
            Optional<TooltipComponent> component, int mouseX, int mouseY) {
        graphics.setTooltipForNextFrame(font,
                com.sighs.sophisticatedsorter.event.ScreenInit.appendTooltipHint(tooltip),
                component, mouseX, mouseY);
    }

    @Override
    public void sophisticatedSorter$setMaximizedPosition(int x, int width) {
        maximizedX = x;
        maximizedWidth = width;
    }
}
