package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.api.IStorageScreenBase;
import com.sighs.sophisticatedsorter.visual.VisualStorageScreen;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value = StorageScreenBase.class, remap = false)
public abstract class StorageScreenBaseMixin implements IStorageScreenBase {
    @Shadow protected abstract void updateSearchFilter(String searchPhrase);

    @Shadow public abstract Predicate<ItemStack> getStackFilter();

    public Predicate<ItemStack> getVisualStackFilter(String searchPhrase) {
        updateSearchFilter(searchPhrase);
        return getStackFilter();
    }

    @Inject(method = "updateDimensionsAndSlotPositions", at = @At("HEAD"), cancellable = true)
    private void no(int pHeight, CallbackInfo ci) {
        if ((Object) this instanceof VisualStorageScreen) ci.cancel();
    }
}
