package com.sighs.sophisticatedsorter.settings;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The default-slot views the container settings use over the real containers of a settings target.
 * On fabric a block-entity container is a vanilla {@link Container}, so the real inventory of a
 * settings target is exposed through {@link ItemHandlerHandle}; the player inventory uses
 * {@link PlayerInventoryHandle}. Both implement the {@link ContainerInventoryHandle} seam.
 */
public final class ContainerInventoryHandles {
	private ContainerInventoryHandles() {
	}

	/** Thin view over a vanilla container (e.g. a chest block entity's inventory). */
	public static final class ItemHandlerHandle implements ContainerInventoryHandle {
		private final Container delegate;

		public ItemHandlerHandle(Container delegate) {
			this.delegate = delegate;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			delegate.setItem(slot, stack);
			delegate.setChanged();
		}

		@Override
		public int getSlots() {
			return delegate.getContainerSize();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return delegate.getItem(slot);
		}

		@Override
		public int getSlotLimit(int slot) {
			return delegate.getMaxStackSize();
		}
	}

	/**
	 * View over the 36 main slots of a player inventory (index 0..35 of {@link Inventory#items}).
	 * Armor and offhand slots are intentionally excluded - container settings target the main
	 * storage slots only, matching the mod's existing container-slot selection rules.
	 */
	public static final class PlayerInventoryHandle implements ContainerInventoryHandle {
		private final Inventory inventory;

		public PlayerInventoryHandle(Inventory inventory) {
			this.inventory = inventory;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			inventory.setItem(slot, stack);
		}

		@Override
		public int getSlots() {
			return Inventory.getSelectionSize();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return inventory.getItem(slot);
		}

		@Override
		public int getSlotLimit(int slot) {
			return inventory.getMaxStackSize(getStackInSlot(slot));
		}
	}
}
