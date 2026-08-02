package com.sighs.sophisticatedsorter.visual;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

import java.util.Optional;

/**
 * The transient menu used by the transfer buttons. It is deliberately empty:
 * the screen exists only as a Core API carrier while a transfer is requested.
 */
public class VisualStorageContainerMenu extends StorageContainerMenuBase {
    protected VisualStorageContainerMenu(Player player) {
        super(MenuType.GENERIC_9x6, player.containerMenu.containerId + 1, player,
                new VisualStorageWrapper(), null, 0, true);
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
    protected StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int slotIndex) {
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
    public boolean stillValid(Player player) {
        return false;
    }

    /**
     * Forge and NeoForge expose this override on Core; Fabric's Core version
     * inherits the vanilla method. Keeping the implementation here avoids a
     * loader-specific menu class while retaining the empty-menu behavior.
     */
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }
}
