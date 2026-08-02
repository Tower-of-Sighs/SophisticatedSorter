package com.sighs.sophisticatedsorter.common;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Shared behavior behind the StorageScreenBase visual-screen bridge. */
public final class StorageScreenPolicy {
    private StorageScreenPolicy() {
    }

    public static <T> Predicate<T> visualStackFilter(
            String searchPhrase,
            Consumer<String> updateSearchFilter,
            Supplier<Predicate<T>> currentFilter) {
        updateSearchFilter.accept(searchPhrase);
        return currentFilter.get();
    }

    public static boolean suppressDimensionUpdate(boolean visualScreen) {
        return visualScreen;
    }

    public static boolean cancelSettingsCategories(boolean visualSettingsHandler) {
        return visualSettingsHandler;
    }

    public static boolean useParentFocus(boolean parentExists) {
        return parentExists;
    }

}
