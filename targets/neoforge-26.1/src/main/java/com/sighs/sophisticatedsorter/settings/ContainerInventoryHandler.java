package com.sighs.sophisticatedsorter.settings;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;

/**
 * A write-through {@link InventoryHandler} over an arbitrary container's real item handler (or the
 * player inventory).
 * <p>
 * <b>Mirror model:</b> the real inventory is canonical for reads, core's internal stacks list is
 * canonical for writes/tracking. Concretely:
 * <ul>
 * <li>{@link #getStackInSlot(int)} reads the real inventory, so everything that renders or
 * inspects the container through the public read API - the settings screen's view-only storage
 * slots, memory-slot selection, item-display previews - shows the live container contents.</li>
 * <li>Every mutation goes through core's own machinery ({@code setStackInSlot} /
 * {@code onContentsChanged}), which keeps the slot tracker, partitioner and save path coherent, and
 * is then pushed into the real inventory via {@link #syncToRealInventory}.</li>
 * </ul>
 * <p>
 * 26.1 rewrite of the 1.21.1 handler: core's constructor now takes a {@link ContainerContents} and
 * the NeoForge 26.1 transfer API replaced the old {@code IItemHandlerModifiable} insert/extract
 * hooks, so the write-through has moved onto {@code setStackInSlot} (public and internal paths) and
 * {@link #onContentsChanged(int, ItemStack)} instead of the removed {@code insertItem}/
 * {@code extractItem} overrides. The 26.1 {@code InventorySorter} also writes through the public
 * {@code setStackInSlot} API (no private stacks-list access anymore), which makes sorting through
 * this wrapper safe - the tracking copy and the real inventory can not diverge.
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
			ContainerContents contents, Runnable saveHandler, StackUpgradeConfig stackUpgradeConfig) {
		super(slots, wrapper, contents, saveHandler, 64, stackUpgradeConfig);
		this.key = key;
		this.realInventory = realInventory;
		this.isPlayer = realInventory instanceof ContainerInventoryHandles.PlayerInventoryHandle;
	}

	public static ContainerInventoryHandler forBlockEntity(int slots, IStorageWrapper wrapper, ContainerSettingsKey key,
			IItemHandlerModifiable realInventory, ContainerContents contents, Runnable saveHandler, StackUpgradeConfig stackUpgradeConfig) {
		return new ContainerInventoryHandler(slots, wrapper, key, realInventory, contents, saveHandler, stackUpgradeConfig);
	}

	public static ContainerInventoryHandler forPlayerInventory(IStorageWrapper wrapper, ContainerSettingsKey key,
			ContainerInventoryHandles.PlayerInventoryHandle realInventory, ContainerContents contents, Runnable saveHandler,
			StackUpgradeConfig stackUpgradeConfig) {
		return new ContainerInventoryHandler(36, wrapper, key, realInventory, contents, saveHandler, stackUpgradeConfig);
	}

	@Override
	protected boolean isAllowed(ItemResource resource) {
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
		// there is no real inventory (client-side block wrapper built from the buffer), fall back to
		// the tracked copy.
		return realInventory != null ? realInventory.getStackInSlot(slot) : super.getStackInSlot(slot);
	}

	/**
	 * Copies this handler's tracked stack for the slot into the real inventory. Called after every
	 * core mutation path ({@code setStackInSlot} / {@code onContentsChanged}).
	 */
	public void syncToRealInventory(int slot) {
		if (realInventory != null) {
			realInventory.setStackInSlot(slot, getInternalStack(slot));
		}
	}

	/**
	 * Re-reads every slot from the real inventory into this handler's tracking list, bypassing the
	 * change-tracking/save machinery. Use after wholesale out-of-band real-inventory changes.
	 */
	public void reload() {
		if (realInventory != null) {
			for (int slot = 0; slot < Math.min(size(), realInventory.getSlots()); slot++) {
				// direct list write, mirrors how core deserializes contents without firing listeners
				setStackInSlotInternal(slot, realInventory.getStackInSlot(slot).copy());
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
	protected void onContentsChanged(int slot, ItemStack stack) {
		// The write-through half of the mirror. During construction this is never reached with a
		// real-inventory pointer present: core's loadStacksFromData fills the stacks list while
		// isInitializing is still true, and that path does not fire onContentsChanged until after
		// the super constructor finishes and this pointer is assigned.
		if (realInventory != null) {
			realInventory.setStackInSlot(slot, getInternalStack(slot));
		}
		super.onContentsChanged(slot, stack);
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