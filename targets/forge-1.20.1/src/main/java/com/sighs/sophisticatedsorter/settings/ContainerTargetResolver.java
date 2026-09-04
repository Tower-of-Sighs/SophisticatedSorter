package com.sighs.sophisticatedsorter.settings;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * Shared server-side resolution of a {@link ContainerSettingsKey} block target to the live block
 * entity and the item handler it exposes. Used by both the settings-screen open/close flow
 * ({@link ContainerOpenFlow}) and the settings-aware sort path ({@link ContainerSettingsSort}).
 * <p>
 * Player-inventory keys are deliberately out of scope: every caller routes those to
 * {@link ContainerSettingsWrapper#playerInventory} before reaching this resolver.
 */
final class ContainerTargetResolver {
	private ContainerTargetResolver() {
	}

	/**
	 * Resolves the block entity for a block key, and the item handler it exposes. Player-inventory
	 * keys return null - callers are expected to handle those first.
	 */
	@Nullable
	static ContainerTarget resolveTarget(ServerPlayer player, ContainerSettingsKey key) {
		if (key.isPlayerInventory()) {
			return null;
		}
		ResourceKey<Level> dimensionKey = key.getDimension();
		Level level = player.level();
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
		// Forge 1.20.1: block-entity capabilities are queried on the block entity itself (Forge
		// auto-attaches the item handler capability to vanilla containers); when absent or not
		// modifiable, fall back to wrapping the block entity's own inventory.
		IItemHandlerModifiable itemHandler = baseContainerBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
				.filter(handler -> handler instanceof IItemHandlerModifiable)
				.map(handler -> (IItemHandlerModifiable) handler)
				.orElse(null);
		if (itemHandler == null) {
			itemHandler = new ContainerInventoryHandles.ItemHandlerHandle(new InvWrapper(baseContainerBlockEntity));
		}
		int slots = itemHandler.getSlots();
		if (slots <= 0) {
			return null;
		}
		Component title = baseContainerBlockEntity.getName();
		return new ContainerTarget(baseContainerBlockEntity, itemHandler, slots, title);
	}

	record ContainerTarget(BaseContainerBlockEntity blockEntity, IItemHandlerModifiable itemHandler, int slots, Component title) {
	}
}