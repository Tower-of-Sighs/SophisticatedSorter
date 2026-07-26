package com.sighs.sophisticatedsorter.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox", remap = false)
public class SearchBoxMixin {
    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"), remap = true)
    private void redirect(StorageScreenBase<?> instance, GuiEventListener guiEventListener) {
        if (instance == null) {
            Minecraft.getInstance().screen.setFocused(guiEventListener);
        } else instance.setFocused(guiEventListener);
    }
}
