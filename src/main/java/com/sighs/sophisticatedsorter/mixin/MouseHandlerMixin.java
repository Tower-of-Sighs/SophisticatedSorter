package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.event.FabricInputEvents;
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
public class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void sophisticatedSorter$onMouseButtonPre(long window, int button, int action, int modifiers, CallbackInfo ci) {
        boolean canceled = FabricInputEvents.MOUSE_BUTTON_PRE.invoker()
                .onMouseButtonPre(new InputEvent.MouseButton.Pre(button, action, modifiers));
        if (canceled) {
            ci.cancel();
        }
    }

    @Inject(method = "onPress", at = @At("TAIL"))
    private void sophisticatedSorter$onMouseButtonPost(long window, int button, int action, int modifiers, CallbackInfo ci) {
        FabricInputEvents.MOUSE_BUTTON_POST.invoker()
                .onMouseButtonPost(new InputEvent.MouseButton.Post(button, action, modifiers));
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void sophisticatedSorter$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        double offset = vertical;
        if (Minecraft.ON_OSX && vertical == 0) {
            offset = horizontal;
        }
        double normalized = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(offset) : offset)
                * this.minecraft.options.mouseWheelSensitivity().get();

        if (this.minecraft.getOverlay() == null && this.minecraft.screen == null && this.minecraft.player != null) {
            MouseHandler self = (MouseHandler) (Object) this;
            InputEvent.MouseScrollingEvent event = new InputEvent.MouseScrollingEvent(
                    normalized,
                    self.isLeftPressed(),
                    self.isMiddlePressed(),
                    self.isRightPressed(),
                    self.xpos(),
                    self.ypos()
            );
            boolean canceled = FabricInputEvents.MOUSE_SCROLL.invoker().onMouseScroll(event);
            if (canceled) {
                ci.cancel();
            }
        }
    }
}