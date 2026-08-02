package com.sighs.sophisticatedsorter.common;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Inert settings behavior shared by every visual settings-handler bridge. */
public final class VisualSettingsBehavior<N, I, R, C, M> {
    private final Supplier<M> globalSettingsFactory;

    public VisualSettingsBehavior(Supplier<M> globalSettingsFactory) {
        this.globalSettingsFactory = globalSettingsFactory;
    }

    public N getSettingsNbtFromContentsNbt(N ignored) {
        return null;
    }

    public void addItemDisplayCategory(Supplier<I> inventoryHandler, Supplier<R> renderInfo, N settingsNbt) {
    }

    public String getGlobalSettingsCategoryName() {
        return "";
    }

    public C instantiateGlobalSettingsCategory(N settingsNbt, Consumer<N> saveHandler) {
        return null;
    }

    public void saveCategoryNbt(N target, String categoryName, N categoryNbt) {
    }

    public M getGlobalSettingsCategory() {
        return globalSettingsFactory.get();
    }
}
