package com.sighs.sophisticatedsorter.settings;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Shared server-side resolution of a {@link ContainerSettingsKey} block target to the live block
 * entity and the item handler it exposes. Used by both the settings-screen open/close flow
 * ({@link ContainerOpenFlow}) and the settings-aware sort path ({@link ContainerSettingsSort}).
 * <p>
 * Player-inventory keys are deliberately out of scope: every caller routes those to
 * {@link ContainerSettingsWrapper#playerInventory} before reaching this resolver.
 * <p>
 * The NeoForge reference resolves the block entity's item handler through its capability system
 * (falling back to wrapping the block entity's own {@code Container}); the fabric port has no
 * such capability, so the block entity's vanilla {@code Container} is wrapped directly - these
 * targets are all vanilla {@code BaseContainerBlockEntity}s, so the two resolve to the same
 * inventories.
 */
final class ContainerTargetResolver {
	private ContainerTargetResolver() {
	}

	/**
	 * Resolves the block entity for a block key, and the item handler it exposes. Player-inventory
	 * keys return null - callers are expected to handle those first.
	 */
		static ContainerTarget resolveTarget(ServerPlayer player, ContainerSettingsKey key) {
		if (key.isPlayerInventory()) {
			return null;
		}
		ResourceKey<Level> dimensionKey = key.getDimension();
		Level level = player.serverLevel();
		if (!level.dimension().equals(dimensionKey)) {
			level = player.server.getLevel(dimensionKey);
		}
		if (!(level instanceof ServerLevel serverLevel)) {
			return null;
		}
		BlockPos pos = key.getPos();
		if (!serverLevel.isLoaded(pos)) {
			return null;
		}
		BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
		if (!(blockEntity instanceof BaseContainerBlockEntity baseContainerBlockEntity)) {
			return null;
		}
		ContainerInventoryHandle itemHandler =
				new ContainerInventoryHandles.ItemHandlerHandle(baseContainerBlockEntity);
		int slots = itemHandler.getSlots();
		if (slots <= 0) {
			return null;
		}
		Component title = baseContainerBlockEntity.getName();
		return new ContainerTarget(baseContainerBlockEntity, itemHandler, slots, title);
	}

	record ContainerTarget(BaseContainerBlockEntity blockEntity,
			ContainerInventoryHandle itemHandler, int slots, Component title) {
	}
}