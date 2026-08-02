package com.sighs.sophisticatedsorter.common;

import java.util.Optional;
import java.util.Set;

/** Shared behavior of the transient, non-interactive visual menu. */
public final class VisualContainerPolicy {
    private static final VisualContainerBehavior<Object, Object, Object, Object> DEFAULT =
            new VisualContainerBehavior<Object, Object, Object, Object>(() -> null);

    private VisualContainerPolicy() {
    }

    public static boolean isStillValid() {
        return DEFAULT.stillValid();
    }

    public static boolean storageItemHasChanged() {
        return DEFAULT.storageItemHasChanged();
    }

    public static boolean detectSettingsChangeAndReload() {
        return DEFAULT.detectSettingsChangeAndReload();
    }

    public static int noSlotCount() {
        return DEFAULT.noSlotCount();
    }

    public static <T> Optional<T> noPosition() {
        return Optional.empty();
    }

    public static <T> T noUpgradeSlot() {
        return null;
    }

    public static Set<Integer> noSortSlotIndexes() {
        return DEFAULT.noSortSlotIndexes();
    }

    public static boolean shouldSuppressSlotPopulation(boolean visualMenu) {
        return DEFAULT.shouldSuppressSlotPopulation(visualMenu);
    }

}
