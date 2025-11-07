package com.sighs.sophisticatedsorter.mixin;

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
    private static int modifySlotLimit(int slotLimit,
                                       ItemStackKey current) {
        // 物品的最大堆叠大小
        int maxStackSize = current.getStack().getMaxStackSize();

        // 返回槽位限制和物品最大堆叠大小的较小值
        return Math.min(slotLimit, maxStackSize);
    }
}
