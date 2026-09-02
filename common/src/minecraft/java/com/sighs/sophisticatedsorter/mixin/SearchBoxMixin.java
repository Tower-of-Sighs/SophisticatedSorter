package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.common.StorageScreenPolicy;
import com.sighs.sophisticatedsorter.utils.SearchBoxPositionAccess;
import com.sighs.sophisticatedsorter.utils.TooltipHints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox", remap = false)
public class SearchBoxMixin implements SearchBoxPositionAccess {
    @Shadow @Final @Mutable private int maximizedX;
    @Shadow @Final @Mutable private int maximizedWidth;

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"), remap = true)
    private void redirect(StorageScreenBase<?> instance, GuiEventListener guiEventListener) {
        if (!StorageScreenPolicy.useParentFocus(instance != null)) {
            Minecraft.getInstance().screen.setFocused(guiEventListener);
        } else {
            instance.setFocused(guiEventListener);
        }
    }

    @Redirect(method = "renderTooltip",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"),
            remap = true)
    private void appendTooltipHint(GuiGraphics guiGraphics, Font font, List<Component> tooltip,
                                   Optional<TooltipComponent> component, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(font, TooltipHints.appendTooltipHint(tooltip), component, mouseX, mouseY);
    }

    @Override
    public void sophisticatedSorter$setMaximizedPosition(int x, int width) {
        maximizedX = x;
        maximizedWidth = width;
    }
}
