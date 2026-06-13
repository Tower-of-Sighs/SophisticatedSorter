package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = InventorySorter.class, remap = false)
public class InventorySorterMixin {
    @ModifyVariable(
            method = "placeStack(Lnet/p3pp3rf1y/sophisticatedcore/inventory/ItemStackKey;IIZLnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$IStackLimitGetter;Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$ISlotStackGetter;Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$ISlotStackSetter;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$IStackLimitGetter;getStackLimit(ILnet/minecraft/world/item/ItemStack;)I",
                    shift = At.Shift.AFTER
            ),
            ordinal = 0,
            argsOnly = true)
    private static int modifySlotLimit(int slotLimit, ItemStackKey current) {
        if (!CoreUtils.shouldLimitToItemMaxStackSize()) {
            return slotLimit;
        }

        int maxStackSize = current.getStack().getMaxStackSize();
        return Math.min(slotLimit, maxStackSize);
    }
}
