package com.sighs.sophisticatedsorter.settings;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Two small adapters that make an arbitrary container's real item handler or the player inventory
 * look like a {@link IItemHandlerModifiable} so the write-through inventory handler in
 * {@link ContainerInventoryHandler} has one uniform view of "the real slots".
 */
public final class ContainerInventoryHandles {
	private ContainerInventoryHandles() {
	}

	/** Thin view over any modifiable item handler, e.g. a block entity's item capability. */
	public static final class ItemHandlerHandle implements IItemHandlerModifiable {
		private final IItemHandlerModifiable delegate;

		public ItemHandlerHandle(IItemHandlerModifiable delegate) {
			this.delegate = delegate;
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			delegate.setStackInSlot(slot, stack);
		}

		@Override
		public int getSlots() {
			return delegate.getSlots();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return delegate.getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return delegate.insertItem(slot, stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return delegate.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return delegate.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return delegate.isItemValid(slot, stack);
		}
	}

	/**
	 * View over the 36 main slots of a player inventory (index 0..35 of {@link Inventory#items}).
	 * Armor and offhand slots are intentionally excluded - container settings target the main
	 * storage slots only, matching the mod's existing container-slot selection rules.
	 */
	public static final class PlayerInventoryHandle implements IItemHandlerModifiable {
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
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			ItemStack existing = getStackInSlot(slot);
			int space = Math.max(0, getSlotLimit(slot) - existing.getCount());
			if (space == 0 || stack.isEmpty()) {
				return stack;
			}
			boolean compatible = existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, stack);
			if (!compatible) {
				return stack;
			}
			int accepted = Math.min(space, stack.getCount());
			if (!simulate) {
				ItemStack merged = existing.copy();
				merged.setCount(existing.getCount() + accepted);
				setStackInSlot(slot, merged);
			}
			return accepted == stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack existing = getStackInSlot(slot);
			if (existing.isEmpty()) {
				return ItemStack.EMPTY;
			}
			int toExtract = Math.min(amount, existing.getCount());
			ItemStack extracted = existing.copyWithCount(toExtract);
			if (!simulate) {
				if (toExtract == existing.getCount()) {
					setStackInSlot(slot, ItemStack.EMPTY);
				} else {
					setStackInSlot(slot, existing.copyWithCount(existing.getCount() - toExtract));
				}
			}
			return extracted;
		}

		@Override
		public int getSlotLimit(int slot) {
			return inventory.getMaxStackSize(getStackInSlot(slot));
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return true;
		}
	}
}