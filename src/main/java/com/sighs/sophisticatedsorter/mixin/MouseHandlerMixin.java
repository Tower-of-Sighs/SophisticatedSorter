package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.event.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow private double xpos;
    @Shadow private double ypos;
    @Shadow public abstract boolean isLeftPressed();
    @Shadow public abstract boolean isMiddlePressed();
    @Shadow public abstract boolean isRightPressed();
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "onPress",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Options;touchscreen()Lnet/minecraft/client/OptionInstance;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private void sophisticatedSorter$onMouseButtonPre(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow()) {
            return;
        }
        boolean cancel = InputEvent.MOUSE_BUTTON_PRE.invoker().onMouseButtonPre(button, action, modifiers);
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "onPress", at = @At("TAIL"))
    private void sophisticatedSorter$onMouseButtonPost(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow()) {
            return;
        }
        InputEvent.MOUSE_BUTTON_POST.invoker().onMouseButtonPost(button, action, modifiers);
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void diligentstalker$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow()) {
            return;
        }
        double offset = (Minecraft.ON_OSX && yOffset == 0) ? xOffset : yOffset;
        if (InputEvent.MOUSE_SCROLL.invoker().onMouseScroll(
                offset, xOffset, yOffset,
                isLeftPressed(), isMiddlePressed(), isRightPressed()
        )) {
            ci.cancel();
        }
    }
}