package com.sighs.sophisticatedsorter.common;

/** Shared stack-limit rule used by each Core InventorySorter descriptor. */
public final class SortStackLimitPolicy {
    private SortStackLimitPolicy() {
    }

    public static int apply(int slotLimit, int itemMaxStackSize, boolean enabled) {
        return enabled ? Math.min(slotLimit, itemMaxStackSize) : slotLimit;
    }
}
