package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.common.SortExecutionState;
import com.sighs.sophisticatedsorter.common.SortStackLimitPolicy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = InventorySorter.class, remap = false)
public class InventorySorterMixin {
    @ModifyVariable(
            method = "placeStack(Lnet/p3pp3rf1y/sophisticatedcore/inventory/ItemStackKey;IIZLnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$IStackLimitGetter;Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$ISlotStackGetter;Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$ISlotStackSetter;)I",
            at = @At(value = "INVOKE", target = "Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$IStackLimitGetter;getStackLimit(ILnet/fabricmc/fabric/api/transfer/v1/item/ItemVariant;)I", shift = At.Shift.AFTER),
            ordinal = 0, argsOnly = true, require = 0)
    private static int modifyFabricSlotLimit(int slotLimit, ItemStackKey current) {
        return applySlotLimit(slotLimit, current);
    }

    @ModifyVariable(
            method = "placeStack(Lnet/p3pp3rf1y/sophisticatedcore/inventory/ItemStackKey;IIZLnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$IStackLimitGetter;Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$ISlotStackGetter;Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$ISlotStackSetter;)I",
            at = @At(value = "INVOKE", target = "Lnet/p3pp3rf1y/sophisticatedcore/util/InventorySorter$IStackLimitGetter;getStackLimit(ILnet/minecraft/world/item/ItemStack;)I", shift = At.Shift.AFTER),
            ordinal = 0, argsOnly = true, require = 0)
    private static int modifyItemStackSlotLimit(int slotLimit, ItemStackKey current) {
        return applySlotLimit(slotLimit, current);
    }

    private static int applySlotLimit(int slotLimit, ItemStackKey current) {
        return SortStackLimitPolicy.apply(slotLimit, current.getStack().getMaxStackSize(),
                SortExecutionState.shouldLimitToItemMaxStackSize());
    }
}
