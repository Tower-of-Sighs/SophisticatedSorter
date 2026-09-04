package com.sighs.sophisticatedsorter.settings;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;

/**
 * A write-through {@link InventoryHandler} over an arbitrary container's real item handler (or the
 * player inventory).
 * <p>
 * <b>Mirror model:</b> the real inventory is canonical for reads, core's {@code stacks} list is
 * canonical for writes/tracking. Concretely:
 * <ul>
 * <li>{@link #getStackInSlot(int)} reads the real inventory, so everything that renders or
 * inspects the container through the public read API - the settings screen's view-only storage
 * slots, memory-slot selection, item-display previews - shows the live container contents.</li>
 * <li>Every mutation goes through core's own machinery ({@code setStackInSlot} /
 * {@code insertItem} / {@code extractItem}), which keeps the slot tracker, partitioner and nbt
 * snapshot coherent, and is then pushed into the real inventory via {@link #syncToRealInventory}.</li>
 * </ul>
 * Core's internal mutation paths read the private {@code stacks} list directly, so the public
 * read override cannot corrupt them; and the settings screen never writes to the real inventory
 * behind this handler's back. Only the sorter (later stage) mutates the real inventory directly -
 * it must call {@link #syncToRealInventory(int)} after each slot change so this handler's tracking
 * copy follows (it also drives the file save through core's own {@code onContentsChanged}).
 * <p>
 * The settings storage nbt and the inventory snapshot share the contents tag, and
 * {@link #setPersistent(boolean)} gates whether saves reach disk (via the save handler).
 */
public class ContainerInventoryHandler extends InventoryHandler {
	private final ContainerSettingsKey key;
	private final IItemHandlerModifiable realInventory;
	private final boolean isPlayer;
	/** Whether the save handler (file write) is allowed to run; mirrors core's persistent flag. */
	private boolean persistent = true;

	public ContainerInventoryHandler(int slots, IStorageWrapper wrapper, ContainerSettingsKey key, @Nullable IItemHandlerModifiable realInventory,
			CompoundTag contentsNbt, Runnable saveHandler, StackUpgradeConfig stackUpgradeConfig) {
		super(slots, wrapper, contentsNbt, saveHandler, 64, stackUpgradeConfig);
		this.key = key;
		this.realInventory = realInventory;
		this.isPlayer = realInventory instanceof ContainerInventoryHandles.PlayerInventoryHandle;
	}

	public static ContainerInventoryHandler forBlockEntity(int slots, IStorageWrapper wrapper, ContainerSettingsKey key,
			IItemHandlerModifiable realInventory, CompoundTag contentsNbt, Runnable saveHandler, StackUpgradeConfig stackUpgradeConfig) {
		return new ContainerInventoryHandler(slots, wrapper, key, realInventory, contentsNbt, saveHandler, stackUpgradeConfig);
	}

	public static ContainerInventoryHandler forPlayerInventory(IStorageWrapper wrapper, ContainerSettingsKey key,
			ContainerInventoryHandles.PlayerInventoryHandle realInventory, CompoundTag contentsNbt, Runnable saveHandler,
			StackUpgradeConfig stackUpgradeConfig) {
		return new ContainerInventoryHandler(36, wrapper, key, realInventory, contentsNbt, saveHandler, stackUpgradeConfig);
	}

	@Override
	protected boolean isAllowed(ItemStack stack) {
		return true;
	}

	public ContainerSettingsKey getKey() {
		return key;
	}

	public boolean isPlayerInventory() {
		return isPlayer;
	}

	/** Whether this handler wraps the real item handler of a block entity (or another container). */
	public boolean wrapsRealInventory() {
		return realInventory != null;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		// Real inventory is canonical for reads (see the mirror-model comment on the class). When
		// there is no real inventory (client-side block wrapper built from the pushed open data), fall
		// back to the tracked copy.
		return realInventory != null ? realInventory.getStackInSlot(slot) : super.getStackInSlot(slot);
	}

	/**
	 * Copies this handler's tracked stack for the slot into the real inventory. Called after every
	 * core mutation path ({@code setStackInSlot} / {@code insertItem} / {@code extractItem}).
	 */
	public void syncToRealInventory(int slot) {
		if (realInventory != null) {
			realInventory.setStackInSlot(slot, getSlotStack(slot));
		}
	}

	/**
	 * Re-reads every slot from the real inventory into this handler's tracking list, bypassing the
	 * change-tracking/save machinery. Use after wholesale out-of-band real-inventory changes.
	 */
	public void reload() {
		if (realInventory != null) {
			for (int slot = 0; slot < Math.min(getSlots(), realInventory.getSlots()); slot++) {
				// direct list write, mirrors how core deserializes nbt without firing listeners
				stacks.set(slot, realInventory.getStackInSlot(slot).copy());
			}
			getSlotTracker().refreshSlotIndexesFrom(this);
		}
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		// Core path (partitioner, tracker, save). The real inventory receives the same stack - the
		// write-through half of the mirror - and its own slot-change hook fires.
		super.setStackInSlot(slot, stack);
		syncToRealInventory(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack remaining = super.insertItem(slot, stack, simulate);
		if (!simulate) {
			syncToRealInventory(slot);
		}
		return remaining;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack extracted = super.extractItem(slot, amount, simulate);
		if (!simulate) {
			syncToRealInventory(slot);
		}
		return extracted;
	}

	@Override
	public void onContentsChanged(int slot) {
		// The write-through half of the mirror. During construction this is never reached with a
		// real-inventory pointer present: core's deserializeNBT fills the stacks list while
		// isInitializing is still true, and that path does not fire onContentsChanged until after
		// the super constructor finishes and this pointer is assigned.
		if (realInventory != null) {
			realInventory.setStackInSlot(slot, getSlotStack(slot));
		}
		super.onContentsChanged(slot);
	}

	@Override
	public void setPersistent(boolean persistent) {
		// Core's flag gates the save-to-nbt step in onContentsChanged; the flag here gates the save
		// handler (file write) for the same reasons. When the settings screen is open (later stage)
		// the wrapper is made non-persistent so preview edits never hit the disk.
		super.setPersistent(persistent);
		this.persistent = persistent;
	}
}