package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.visual.VisualStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = StorageContainerMenuBase.class, remap = false)
public class StorageContainerMenuBaseMixin {
    @Inject(method = "getNoSortSlotIndexes", at = @At("HEAD"), cancellable = true)
    private void no(CallbackInfoReturnable<Set<Integer>> cir) {
        if ((Object) this instanceof VisualStorageContainerMenu) cir.setReturnValue(Set.of());
    }

    @Inject(method = {"addStorageInventorySlots", "addUpgradeSlots", "addPlayerInventorySlots", "addUpgradeSettingsContainers"}, at = @At("HEAD"), cancellable = true)
    private void no(CallbackInfo ci) {
        if ((Object) this instanceof VisualStorageContainerMenu) ci.cancel();
    }

    @Inject(method = {"getNumberOfUpgradeSlots", "getNumberOfStorageInventorySlots"}, at = @At("HEAD"), cancellable = true)
    private void nono(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof VisualStorageContainerMenu) cir.setReturnValue(0);
    }
}
