package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.event.FabricInputEvents;
import com.sighs.sophisticatedsorter.event.InputEvent;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("TAIL"))
    private void sophisticatedSorter$onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        FabricInputEvents.KEY.invoker().onKey(new InputEvent.Key(key, scanCode, action, modifiers));
    }
}