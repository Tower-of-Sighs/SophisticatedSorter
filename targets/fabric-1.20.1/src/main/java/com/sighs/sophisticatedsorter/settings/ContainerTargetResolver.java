package com.sighs.sophisticatedsorter.settings;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
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
 * entity and the item storage it exposes. Used by both the settings-screen open/close flow
 * ({@link ContainerOpenFlow}) and the settings-aware sort path ({@link ContainerSettingsSort}).
 * <p>
 * Player-inventory keys are deliberately out of scope: every caller routes those to
 * {@link ContainerSettingsWrapper#playerInventory} before reaching this resolver.
 * <p>
 * Platform adaptation: the 1.21.1 reference resolves the item handler through NeoForge's item
 * capability ({@code CapabilityHelper.getFromItemHandler}, with an {@code InvWrapper} fallback);
 * Fabric 1.20.1 has neither the capability registry nor that helper, so vanilla container block
 * entities are wrapped directly as a {@link SlottedStackStorage} over their {@link Container}.
 */
final class ContainerTargetResolver {
	private ContainerTargetResolver() {
	}

	/**
	 * Resolves the block entity for a block key, and the item storage it exposes. Player-inventory
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
		ContainerInventoryHandles.ContainerHandle storage = new ContainerInventoryHandles.ContainerHandle(baseContainerBlockEntity);
		int slots = storage.getSlotCount();
		if (slots <= 0) {
			return null;
		}
		Component title = baseContainerBlockEntity.getName();
		return new ContainerTarget(baseContainerBlockEntity, storage, slots, title);
	}

	record ContainerTarget(BaseContainerBlockEntity blockEntity, SlottedStackStorage storage, int slots, Component title) {
	}
}