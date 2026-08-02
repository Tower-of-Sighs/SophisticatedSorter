package com.sighs.sophisticatedsorter.common;

/** Shared re-entrant state used while the Sophisticated Core sorter runs. */
public final class SortExecutionState {
    private static final ThreadLocal<Boolean> LIMIT_TO_ITEM_MAX_STACK_SIZE =
            ThreadLocal.withInitial(() -> false);

    private SortExecutionState() {
    }

    public static boolean shouldLimitToItemMaxStackSize() {
        return LIMIT_TO_ITEM_MAX_STACK_SIZE.get();
    }

    public static void withItemMaxStackSizeLimit(Runnable action) {
        LIMIT_TO_ITEM_MAX_STACK_SIZE.set(true);
        try {
            action.run();
        } finally {
            LIMIT_TO_ITEM_MAX_STACK_SIZE.remove();
        }
    }
}
