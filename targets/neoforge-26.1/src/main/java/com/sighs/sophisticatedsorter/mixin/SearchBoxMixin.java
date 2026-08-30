package com.sighs.sophisticatedsorter.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Lets Core's search box work on ordinary screens where its owner is null. */
@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox", remap = false)
public final class SearchBoxMixin {
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
}
