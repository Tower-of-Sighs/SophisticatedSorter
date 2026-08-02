package com.sighs.sophisticatedsorter.utils;

import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The only sorter operation that still depends on the loader/Core storage API.
 * The shared sorter supplies the selected comparator and owns all surrounding
 * sort and transfer behavior.
 */
public interface SortPlatform {
    void sortStacks(List<ItemStack> stacks,
                    Comparator<Map.Entry<ItemStackKey, Integer>> comparator);
}
