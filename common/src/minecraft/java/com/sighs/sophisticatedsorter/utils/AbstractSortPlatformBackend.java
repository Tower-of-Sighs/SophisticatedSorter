package com.sighs.sophisticatedsorter.utils;

import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Shared adapter plumbing around the one loader-specific Core storage type. */
public abstract class AbstractSortPlatformBackend<H> implements SortPlatform {
    protected abstract H createHandler(int size);

    protected abstract void setStack(H handler, int slot, ItemStack stack);

    protected abstract ItemStack getStack(H handler, int slot);

    protected abstract void sortHandler(H handler,
                                        Comparator<Map.Entry<ItemStackKey, Integer>> comparator);

    @Override
    public final void sortStacks(List<ItemStack> stacks,
                                 Comparator<Map.Entry<ItemStackKey, Integer>> comparator) {
        H handler = createHandler(stacks.size());
        for (int index = 0; index < stacks.size(); index++) {
            setStack(handler, index, stacks.get(index));
        }
        com.sighs.sophisticatedsorter.common.SortExecutionState.withItemMaxStackSizeLimit(
                () -> sortHandler(handler, comparator));
        for (int index = 0; index < stacks.size(); index++) {
            stacks.set(index, getStack(handler, index));
        }
    }
}
