package com.sighs.sophisticatedsorter.settings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Resolves the {@link ContainerSettingsKey} a container menu was opened from by matching the menu's
 * first non-player {@link Container} against the block entities around the player. Used both on the
 * server (to record the target of the settings gear) and on the client (to look up the settings of
 * the container the player is currently viewing so slot highlights render on vanilla screens).
 * <p>
 * Only {@link Level} APIs are used so the same code runs on {@link net.minecraft.server.level.ServerLevel}
 * and {@link net.minecraft.client.multiplayer.ClientLevel}.
 */
public final class ContainerSettingsKeyResolver {
	/** Upper bound for the slot-walk when finding the first non-player slot of a menu. */
	private static final int MAX_RESOLVED_CONTAINER_SLOTS = 256;

	private ContainerSettingsKeyResolver() {
	}

	/**
	 * Finds the key of the container the given menu was opened from, relative to the player's
	 * position. Returns null when the menu does not resolve to a supported block entity (including
	 * the player inventory).
	 */
	public static ContainerSettingsKey resolveKey(Player player, AbstractContainerMenu menu) {
		Container storageContainer = findStorageContainer(player, menu);
		if (storageContainer == null || storageContainer instanceof Inventory) {
			return null;
		}
		Level level = player.level();
		BlockPos playerPos = player.blockPosition();
		// Radius 2 in every direction covers every vanilla container (double chest included) plus the
		// reach of a hopper-fed block; keeps the search bounded and cheap.
		int range = 2;
		for (int dx = -range; dx <= range; dx++) {
			for (int dy = -range; dy <= range; dy++) {
				for (int dz = -range; dz <= range; dz++) {
					BlockPos candidate = playerPos.offset(dx, dy, dz);
					BlockEntity blockEntity = level.getBlockEntity(candidate);
					if (blockEntity instanceof BaseContainerBlockEntity baseContainerBlockEntity
							&& isSameContainer(baseContainerBlockEntity, storageContainer)) {
						return ContainerSettingsKey.block(level.dimension(), candidate.immutable());
					}
				}
			}
		}
		return null;
	}

	private static boolean isSameContainer(BaseContainerBlockEntity blockEntity, Container storageContainer) {
		if (blockEntity == storageContainer) {
			return true;
		}
		return storageContainer instanceof CompoundContainer compoundContainer && compoundContainer.contains(blockEntity);
	}

	/**
	 * Walks the non-player slots of the menu and returns the {@link Container} backing its first
	 * storage slot (the "chest inventory" object the {@link Slot} was created over). Player
	 * inventory slots occupy the tail of a menu's slot list, so the first slot whose container is
	 * not the player inventory is the container being viewed.
	 */
	private static Container findStorageContainer(Player player, AbstractContainerMenu menu) {
		Inventory playerInventory = player.getInventory();
		int walked = 0;
		for (Slot slot : menu.slots) {
			if (++walked > MAX_RESOLVED_CONTAINER_SLOTS) {
				break;
			}
			if (playerInventory != null && slot.container == playerInventory) {
				continue;
			}
			if (slot.container != null) {
				return slot.container;
			}
		}
		return null;
	}
}
