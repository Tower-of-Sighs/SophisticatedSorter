package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.visual.VisualSettingsHandler;
import net.minecraft.nbt.CompoundTag;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = SettingsHandler.class, remap = false)
public class SettingsHandlerMixin {
    @Inject(method = "addSettingsCategories", at = @At("HEAD"), cancellable = true)
    private void cancel(Supplier<InventoryHandler> inventoryHandlerSupplier, Supplier<RenderInfo> renderInfoSupplier, CompoundTag settingsNbt, CallbackInfo ci) {
        if ((Object) this instanceof VisualSettingsHandler) ci.cancel();
    }
}
