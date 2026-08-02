package com.sighs.sophisticatedsorter.common;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Complete inert implementation of the Core storage-wrapper contract.
 *
 * <p>The type parameters are the loader's Core/Minecraft types.  Keeping them
 * generic lets the behavior compile as Java 8 without importing any platform
 * API while targets only provide their concrete type bridge.</p>
 */
public abstract class AbstractVisualStorageWrapper<UpgradeInventory, Inventory, InputOutput,
        Settings, Upgrade, Sort, Player, RenderInfo, DisplayName>
        {
    private final Supplier<Settings> settingsFactory;

    protected AbstractVisualStorageWrapper(Supplier<Settings> settingsFactory) {
        this.settingsFactory = settingsFactory;
    }

    public final void setContentsChangeHandler(Runnable handler) {
    }

    public final UpgradeInventory getInventoryForUpgradeProcessing() {
        return null;
    }

    public final Inventory getInventoryHandler() {
        return null;
    }

    public final InputOutput getInventoryForInputOutput() {
        return null;
    }

    public final Settings getSettingsHandler() {
        return settingsFactory.get();
    }

    public final Upgrade getUpgradeHandler() {
        return null;
    }

    public final Optional<UUID> getContentsUuid() {
        return Optional.empty();
    }

    public final Optional<Integer> getOpenTabId() {
        return Optional.empty();
    }

    public final void setOpenTabId(int tabId) {
    }

    public final void removeOpenTabId() {
    }

    public final void setSortBy(Sort sortBy) {
    }

    public final Sort getSortBy() {
        return null;
    }

    public final void sort() {
    }

    public final void onContentsNbtUpdated() {
    }

    public final void refreshInventoryForUpgradeProcessing() {
    }

    public final void refreshInventoryForInputOutput() {
    }

    public final void setPersistent(boolean persistent) {
    }

    public final void fillWithLoot(Player player) {
    }

    public final RenderInfo getRenderInfo() {
        return null;
    }

    public final void setColumnsTaken(int columns, boolean hasChanged) {
    }

    public final int getColumnsTaken() {
        return 0;
    }

    public final String getStorageType() {
        return "";
    }

    public final DisplayName getDisplayName() {
        return null;
    }

    public final int getMainColor() {
        return 0;
    }

    public final int getAccentColor() {
        return 0;
    }

    public final void setColors(int mainColor, int accentColor) {
    }
}
