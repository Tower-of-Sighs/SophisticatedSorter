package com.sighs.sophisticatedsorter.utils;

import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** Fabric 1.20.1's Core storage bridge uses the transfer SlottedStackStorage API. */
public final class PlatformSortBackend extends AbstractSortPlatformBackend<SimpleSlottedStackStorage> {
    public static final SortPlatform INSTANCE = new PlatformSortBackend();

    private PlatformSortBackend() {
    }

    @Override
    protected SimpleSlottedStackStorage createHandler(int size) {
        return new SimpleSlottedStackStorage(size);
    }

    @Override
    protected void setStack(SimpleSlottedStackStorage handler, int slot, ItemStack stack) {
        handler.setStackInSlot(slot, stack);
    }

    @Override
    protected ItemStack getStack(SimpleSlottedStackStorage handler, int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    protected void sortHandler(SimpleSlottedStackStorage handler,
                               Comparator<Map.Entry<ItemStackKey, Integer>> comparator) {
        InventorySorter.sortHandler(handler, comparator, new HashSet<Integer>());
    }
}
