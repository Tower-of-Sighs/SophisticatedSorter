package com.sighs.sophisticatedsorter.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** Forge 1.20.1's only sorter API difference: Forge's item handler type. */
public final class PlatformSortBackend extends AbstractSortPlatformBackend<ItemStackHandler> {
    public static final SortPlatform INSTANCE = new PlatformSortBackend();

    private PlatformSortBackend() {
    }

    @Override
    protected ItemStackHandler createHandler(int size) {
        return new ItemStackHandler(size);
    }

    @Override
    protected void setStack(ItemStackHandler handler, int slot, ItemStack stack) {
        handler.setStackInSlot(slot, stack);
    }

    @Override
    protected ItemStack getStack(ItemStackHandler handler, int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    protected void sortHandler(ItemStackHandler handler,
                               Comparator<Map.Entry<ItemStackKey, Integer>> comparator) {
        InventorySorter.sortHandler(handler, comparator, new HashSet<Integer>());
    }
}
