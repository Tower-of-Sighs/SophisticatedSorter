package com.sighs.sophisticatedsorter.utils;

import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** NeoForge 26.1 sorter bridge. Core now sorts its InventoryHandler directly,
 * so the player-list adapter performs the same grouped-stack ordering locally. */
public final class PlatformSortBackend implements SortPlatform {
    public static final SortPlatform INSTANCE = new PlatformSortBackend();

    private PlatformSortBackend() {
    }

    @Override
    public void sortStacks(List<ItemStack> stacks,
                           Comparator<Map.Entry<ItemStackKey, Integer>> comparator) {
        Map<ItemStackKey, Integer> counts = new LinkedHashMap<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                counts.merge(ItemStackKey.of(stack), stack.getCount(), Integer::sum);
            }
        }
        List<Map.Entry<ItemStackKey, Integer>> ordered = new ArrayList<>(counts.entrySet());
        ordered.sort(comparator);
        List<ItemStack> result = new ArrayList<>(stacks.size());
        for (Map.Entry<ItemStackKey, Integer> entry : ordered) {
            int remaining = entry.getValue();
            ItemStack template = entry.getKey().stack();
            while (remaining > 0 && result.size() < stacks.size()) {
                int amount = Math.min(remaining, template.getMaxStackSize());
                result.add(template.copyWithCount(amount));
                remaining -= amount;
            }
        }
        while (result.size() < stacks.size()) {
            result.add(ItemStack.EMPTY);
        }
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, result.get(i));
        }
    }
}
