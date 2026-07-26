package com.sighs.sophisticatedsorter.visual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

import java.util.Optional;

public class VisualStorageContainerMenu extends StorageContainerMenuBase {
    protected VisualStorageContainerMenu(Player player) {
        super(MenuType.GENERIC_9x6, player.containerMenu.containerId + 1, player, new VisualStorageWrapper(), null, 0, true);
    }

    @Override
    public Optional<BlockPos> getBlockPosition() {
        return Optional.empty();
    }

    @Override
    public Optional<Entity> getEntity() {
        return Optional.empty();
    }

    @Override
    protected StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int i) {
        return null;
    }

    @Override
    public void openSettings() {

    }

    @Override
    protected boolean storageItemHasChanged() {
        return false;
    }

    @Override
    public boolean detectSettingsChangeAndReload() {
        return false;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return false;
    }
}
