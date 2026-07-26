package com.sighs.sophisticatedsorter.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure selection rules shared by every loader-specific menu adapter. */
public final class ContainerSelection {
    private ContainerSelection() {
    }

    public static List<Integer> playerMainInventorySlots(int inventorySize) {
        int end = Math.min(inventorySize, 36);
        if (end <= 9) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<Integer>(end - 9);
        for (int index = 9; index < end; index++) {
            result.add(index);
        }
        return result;
    }

    public static <T> List<T> withoutTrailingHotbar(List<T> slots, int hotbarSize) {
        int end = Math.max(0, slots.size() - Math.max(0, hotbarSize));
        return new ArrayList<T>(slots.subList(0, end));
    }
}
