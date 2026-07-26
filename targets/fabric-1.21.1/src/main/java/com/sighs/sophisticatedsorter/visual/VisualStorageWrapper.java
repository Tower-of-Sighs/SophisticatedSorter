package com.sighs.sophisticatedsorter.visual;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

import java.util.Optional;
import java.util.UUID;

public class VisualStorageWrapper implements IStorageWrapper {
    @Override
    public void setContentsChangeHandler(Runnable runnable) {

    }

    @Override
    public ITrackedContentsItemHandler getInventoryForUpgradeProcessing() {
        return null;
    }

    @Override
    public InventoryHandler getInventoryHandler() {
        return null;
    }

    @Override
    public ITrackedContentsItemHandler getInventoryForInputOutput() {
        return null;
    }

    @Override
    public SettingsHandler getSettingsHandler() {
        return new VisualSettingsHandler();
    }

    @Override
    public UpgradeHandler getUpgradeHandler() {
        return null;
    }

    @Override
    public Optional<UUID> getContentsUuid() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getOpenTabId() {
        return Optional.empty();
    }

    @Override
    public void setOpenTabId(int i) {

    }

    @Override
    public void removeOpenTabId() {

    }

    @Override
    public void setSortBy(SortBy sortBy) {

    }

    @Override
    public SortBy getSortBy() {
        return null;
    }

    @Override
    public void sort() {

    }

    @Override
    public void onContentsNbtUpdated() {

    }

    @Override
    public void refreshInventoryForUpgradeProcessing() {

    }

    @Override
    public void refreshInventoryForInputOutput() {

    }

    @Override
    public void setPersistent(boolean b) {

    }

    @Override
    public void fillWithLoot(Player player) {

    }

    @Override
    public RenderInfo getRenderInfo() {
        return null;
    }

    @Override
    public void setColumnsTaken(int i, boolean b) {

    }

    @Override
    public int getColumnsTaken() {
        return 0;
    }

    @Override
    public String getStorageType() {
        return "";
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public int getMainColor() {
        return 0;
    }

    @Override
    public int getAccentColor() {
        return 0;
    }

    @Override
    public void setColors(int i, int i1) {

    }
}
