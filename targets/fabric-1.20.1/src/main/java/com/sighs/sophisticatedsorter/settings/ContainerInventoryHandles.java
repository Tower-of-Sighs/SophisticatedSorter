package com.sighs.sophisticatedsorter.settings;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import java.util.Collections;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Two small adapters that make an arbitrary container's real item storage or the player inventory
 * look like the {@link SlottedStackStorage} this core uses for its item-transfer APIs, so the
 * write-through inventory handler in {@link ContainerInventoryHandler} and the settings-aware sort
 * in {@link ContainerSettingsSort} have one uniform view of "the real slots".
 * <p>
 * This is the Fabric 1.20.1 counterpart of the reference's {@code IItemHandlerModifiable}-based
 * handles: Fabric has no item-capability registry, so block entities are wrapped through the
 * vanilla {@link Container} interface directly instead of a capability lookup.
 */
public final class ContainerInventoryHandles {
	private ContainerInventoryHandles() {
	}

	private abstract static class BaseHandle implements SlottedStackStorage {
		protected abstract ItemStack stackInSlot(int slot);

		protected abstract void setSlotStack(int slot, ItemStack stack);

		protected abstract int slotLimit(int slot);

		protected abstract int slotCount();

		@Override
		public ItemStack getStackInSlot(int slot) {
			return stackInSlot(slot);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			setSlotStack(slot, stack);
		}

		@Override
		public int getSlotLimit(int slot) {
			return slotLimit(slot);
		}

		@Override
		public int getSlotCount() {
			return slotCount();
		}

		@Override
		public SingleSlotStorage<ItemVariant> getSlot(int slot) {
			return new SlotHandle(slot);
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			// All writes to the real inventory go through setStackInSlot; the transfer-API insert
			// path is never used on these handles.
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			return 0;
		}

		private final class SlotHandle implements SingleSlotStorage<ItemVariant> {
			private final int index;

			private SlotHandle(int index) {
				this.index = index;
			}

			@Override
			public boolean isResourceBlank() {
				return BaseHandle.this.stackInSlot(index).isEmpty();
			}

			@Override
			public ItemVariant getResource() {
				return ItemVariant.of(BaseHandle.this.stackInSlot(index));
			}

			@Override
			public long getAmount() {
				return BaseHandle.this.stackInSlot(index).getCount();
			}

			@Override
			public long getCapacity() {
				return BaseHandle.this.slotLimit(index);
			}

			@Override
			public StorageView<ItemVariant> getUnderlyingView() {
				return this;
			}

			@Override
			public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
				ItemStack current = BaseHandle.this.stackInSlot(index);
				if (!resource.matches(current)) {
					return 0;
				}
				int existing = current.getCount();
				int canInsert = (int) Math.min(Math.max((long) BaseHandle.this.slotLimit(index) - existing, 0), Math.max(maxAmount, 0));
				if (canInsert <= 0) {
					return 0;
				}
				BaseHandle.this.setSlotStack(index, resource.toStack(existing + canInsert));
				return canInsert;
			}

			@Override
			public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
				ItemStack current = BaseHandle.this.stackInSlot(index);
				if (current.isEmpty() || !resource.matches(current)) {
					return 0;
				}
				int extracted = (int) Math.min(current.getCount(), Math.max(maxAmount, 0));
				int remaining = current.getCount() - extracted;
				if (remaining <= 0) {
					BaseHandle.this.setSlotStack(index, ItemStack.EMPTY);
				} else {
					ItemStack updated = current.copy();
					updated.setCount(remaining);
					BaseHandle.this.setSlotStack(index, updated);
				}
				return extracted;
			}

			@Override
			public Iterator<StorageView<ItemVariant>> iterator() {
				return Collections.<StorageView<ItemVariant>>singletonList(this).iterator();
			}
		}
	}

	/**
	 * View over a vanilla {@link Container} (e.g. a block entity's own inventory) as a
	 * {@link SlottedStackStorage}.
	 */
	public static final class ContainerHandle extends BaseHandle {
		private final Container container;

		public ContainerHandle(Container container) {
			this.container = container;
		}

		@Override
		protected ItemStack stackInSlot(int slot) {
			return container.getItem(slot);
		}

		@Override
		protected void setSlotStack(int slot, ItemStack stack) {
			container.setItem(slot, stack == null ? ItemStack.EMPTY : stack);
		}

		@Override
		protected int slotLimit(int slot) {
			return container.getMaxStackSize();
		}

		@Override
		protected int slotCount() {
			return container.getContainerSize();
		}
	}

	/**
	 * View over the 36 main slots of a player inventory (index 0..35 of {@link Inventory#items}).
	 * Armor and offhand slots are intentionally excluded - container settings target the main
	 * storage slots only, matching the mod's existing container-slot selection rules.
	 */
	public static final class PlayerInventoryHandle extends BaseHandle {
		private final Inventory inventory;

		public PlayerInventoryHandle(Inventory inventory) {
			this.inventory = inventory;
		}

		@Override
		protected ItemStack stackInSlot(int slot) {
			return inventory.getItem(slot);
		}

		@Override
		protected void setSlotStack(int slot, ItemStack stack) {
			inventory.setItem(slot, stack == null ? ItemStack.EMPTY : stack);
		}

		@Override
		protected int slotLimit(int slot) {
			return inventory.getMaxStackSize();
		}

		@Override
		protected int slotCount() {
			return Inventory.getSelectionSize();
		}
	}
}