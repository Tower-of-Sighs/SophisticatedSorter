package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.common.VisualContainerPolicy;
import com.sighs.sophisticatedsorter.visual.VisualStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Core exposes this hook on Fabric and Forge, but not on the current NeoForge build.
 * The optional injection keeps the shared behavior in common and is a no-op there.
 */
@Mixin(value = StorageContainerMenuBase.class, remap = false)
public class StorageContainerMenuBaseNoSortMixin {
    @Inject(method = "getNoSortSlotIndexes", at = @At("HEAD"), cancellable = true, require = 0)
    private void emptyVisualNoSortSlots(CallbackInfoReturnable<Set<Integer>> cir) {
        if (VisualContainerPolicy.shouldSuppressSlotPopulation((Object) this instanceof VisualStorageContainerMenu)) {
            cir.setReturnValue(VisualContainerPolicy.noSortSlotIndexes());
        }
    }
}
