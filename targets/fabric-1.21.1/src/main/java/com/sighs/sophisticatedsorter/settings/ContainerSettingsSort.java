package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.common.SortExecutionState;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SortTarget;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsCategory;

/**
 * Settings-aware server-side sort for arbitrary containers and the player inventory.
 * <p>
 * When the target of a sort request has a per-container settings record that carries no-sort or
 * memory slot selections, sorting is done <b>in place</b> with core's own settings-aware sorter
 * (the {@code InventorySorter.sortHandler} overload with no-sort slots, memorized slots and a slot
 * filter) so that:
 * <ul>
 * <li>no-sort slots keep whatever they hold and are excluded from compaction, and</li>
 * <li>memory slots are emptied first and then filled (in slot order) with the items whose filter
 * they match.</li>
 * </ul>
 * When the target has no settings record, or none of the settings are meaningful for sorting, the
 * method returns {@code false} and the caller falls back to the mod's generic copy-based sort, so
 * plain containers keep their existing behavior.
 * <p>
 * <b>Sorting target vs. settings wrapper:</b> the settings are read through a transient
 * {@link ContainerSettingsWrapper} (the same facade the settings screen uses), but the sort itself
 * runs over the target's <i>real</i> item handler / the player's real {@code Inventory}, not over
 * the wrapper's {@link ContainerInventoryHandler}. Core's {@code InventorySorter} detects a core
 * {@code InventoryHandler} and then writes through the low-level slot-stack methods - and never
 * invokes the write-through {@code syncToRealInventory} half of {@link ContainerInventoryHandler},
 * so sorting through the wrapper would leave its tracking copy and the real inventory out of sync
 * (and the tracking nbt would later be written back over the sorted real contents). The real
 * handlers are plain {@link ContainerInventoryHandle} views, so the
 * sorter takes the direct slot-read/write path against the single canonical source of truth. The
 * settings file only ever stores settings nbt; container contents are saved through the block
 * entity's own save path.
 * <p>
 * <b>Fabric port difference:</b> the fabric build of Sophisticated Core that this target compiles
 * against (3.1.0-beta.47) only ships the plain
 * {@code InventorySorter.sortHandler(handler, comparator, noSortSlots)} overload - the
 * memory-slot-aware overload does not exist there yet. The whole settings-aware pass therefore is
 * reimplemented here against {@link ContainerInventoryHandle} primitives,
 * faithfully mirroring the NeoForge core algorithm (compacted pool, memorized-slot pass, then the
 * plain no-sort pass over the leftover entries).
 */
public final class ContainerSettingsSort {
	private ContainerSettingsSort() {
	}

	/**
	 * Attempts a settings-aware in-place sort for the given request. Only container and
	 * player-inventory targets can have settings; anything else falls through to the caller.
	 *
	 * @return true when the request was handled settings-aware, false when the caller should run
	 *         the generic sort instead
	 */
	public static boolean trySortSettingsAware(ServerPlayer player, SortRequest request) {
		if (request == null || request.target() != SortTarget.CONTAINER) {
			return false;
		}
		ContainerSettingsKey key = ContainerSettingsTracker.get().getOpenKey(player);
		if (key == null) {
			return false;
		}
		ContainerSettingsStore storage = ServerContainerSettingsStore.get();
		if (storage == null) {
			return false;
		}
		if (key.isPlayerInventory()) {
			ContainerInventoryHandles.PlayerInventoryHandle realInventory =
					new ContainerInventoryHandles.PlayerInventoryHandle(player.getInventory());
			ContainerSettingsWrapper wrapper = ContainerSettingsWrapper.playerInventory(storage, key, realInventory);
			return sortSettingsAware(player, request, wrapper, realInventory, null);
		}
		ContainerTargetResolver.ContainerTarget target = ContainerTargetResolver.resolveTarget(player, key);
		if (target == null) {
			return false;
		}
		ContainerSettingsWrapper wrapper = new ContainerSettingsWrapper(storage, key, target.slots(), target.itemHandler(),
				target.title());
		return sortSettingsAware(player, request, wrapper, target.itemHandler(), target.blockEntity());
	}

	/**
	 * Runs the settings-aware sort when the target's settings are meaningful. A target whose
	 * settings record is completely empty is deliberately left to the generic sort so plain
	 * containers (double chests included) are not disturbed by a half-sorted in-place pass.
	 */
	private static boolean sortSettingsAware(ServerPlayer player, SortRequest request, ContainerSettingsWrapper wrapper,
			ContainerInventoryHandle sortTarget, BlockEntity blockEntity) {
		Set<Integer> noSortSlots = new HashSet<>(wrapper.getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class).getNoSortSlots());
		MemorySettingsCategory memory = wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);
		Set<Integer> memorySlots = new HashSet<>(memory.getSlotIndexes());
		memorySlots.removeAll(noSortSlots);
		if (noSortSlots.isEmpty() && memorySlots.isEmpty()) {
			return false;
		}
		// The mod's sort criterion travels from the client; wrapper.getSortBy() is the container
		// settings' own preference and stays at its NAME default for these targets. Both are resolved
		// through the same comparator selection core's own menus use.
		Comparator<Map.Entry<ItemStackKey, Integer>> comparator = CoreUtils.getComparator(wrapper.getSortBy(), request.pinyinOrder());
		SortExecutionState.withItemMaxStackSizeLimit(() -> {
			Map<ItemStackKey, Integer> compacted = getCompactedStacks(sortTarget, noSortSlots);
			List<Map.Entry<ItemStackKey, Integer>> entries = new ArrayList<>(compacted.entrySet());
			entries.sort(comparator);
			sortIntoMemorizedSlots(sortTarget, memorySlots, memory::matchesFilter, entries);
			noSortSlots.addAll(memorySlots);
			sortIntoOtherSlots(sortTarget, noSortSlots, entries, sortTarget.getSlots());
		});
		if (blockEntity != null) {
			// Persist through the block entity's own save path - the sorted contents never enter the
			// settings storage.
			blockEntity.setChanged();
		}
		// The player still has the vanilla container menu open for this block (the settings screen is
		// a different menu); push the in-place slot changes out to the open screen.
		player.containerMenu.broadcastChanges();
		return true;
	}

	/** Aggregates the contents of every slot not in the excluded set by item key (core's own compaction). */
	private static Map<ItemStackKey, Integer> getCompactedStacks(ContainerInventoryHandle handler,
			Set<Integer> excludedSlots) {
		Map<ItemStackKey, Integer> ret = new HashMap<>();
		for (int slot = 0; slot < handler.getSlots(); slot++) {
			if (excludedSlots.contains(slot)) {
				continue;
			}
			ItemStack stack = handler.getStackInSlot(slot);
			if (stack.isEmpty()) {
				continue;
			}
			ret.merge(ItemStackKey.of(stack), stack.getCount(), Integer::sum);
		}
		return ret;
	}

	/**
	 * Empties every memorized slot, then walks the sorted pool in slot order and places each entry
	 * into the first memorized slots whose filter it matches. Entries that were placed completely
	 * are removed from the pool; the rest keep their remaining count for the general pass. Mirrors
	 * core's {@code sortIntoMemorizedSlots}.
	 */
	private static void sortIntoMemorizedSlots(ContainerInventoryHandle handler, Set<Integer> memorizedSlots,
			BiPredicate<Integer, ItemStack> slotFilter, List<Map.Entry<ItemStackKey, Integer>> entries) {
		if (memorizedSlots.isEmpty()) {
			return;
		}
		List<Integer> orderedSlots = memorizedSlots.stream().sorted().toList();
		for (int slot : orderedSlots) {
			emptySlot(handler, slot);
		}
		Iterator<Map.Entry<ItemStackKey, Integer>> it = entries.iterator();
		while (it.hasNext()) {
			Map.Entry<ItemStackKey, Integer> entry = it.next();
			ItemStackKey key = entry.getKey();
			int count = entry.getValue();
			for (int slot : orderedSlots) {
				if (count <= 0) {
					break;
				}
				if (!handler.getStackInSlot(slot).isEmpty()) {
					continue;
				}
				if (!slotFilter.test(slot, key.getStack())) {
					continue;
				}
				count -= placeStack(handler, key, count, slot);
			}
			if (count <= 0) {
				it.remove();
			} else {
				entry.setValue(count);
			}
		}
	}

	/**
	 * Fills the non-excluded slots from the sorted pool in slot order, emptying the leftover slots.
	 * Mirrors core's {@code sortIntoOtherSlots}.
	 */
	private static void sortIntoOtherSlots(ContainerInventoryHandle handler, Set<Integer> noSortSlots,
			List<Map.Entry<ItemStackKey, Integer>> entries, int slotCount) {
		Iterator<Map.Entry<ItemStackKey, Integer>> it = entries.iterator();
		ItemStackKey currentKey = null;
		int currentCount = 0;
		for (int slot = 0; slot < slotCount; slot++) {
			if (noSortSlots.contains(slot)) {
				continue;
			}
			if (currentKey == null || currentCount <= 0) {
				if (it.hasNext()) {
					Map.Entry<ItemStackKey, Integer> entry = it.next();
					currentKey = entry.getKey();
					currentCount = entry.getValue();
				}
			}
			if (currentKey == null || currentCount <= 0) {
				emptySlot(handler, slot);
				continue;
			}
			currentCount -= placeStack(handler, currentKey, currentCount, slot);
		}
	}

	/** Removes whatever the slot currently holds (core's {@code emptySlot}). */
	private static void emptySlot(ContainerInventoryHandle handler, int slot) {
		if (!handler.getStackInSlot(slot).isEmpty()) {
			handler.setStackInSlot(slot, ItemStack.EMPTY);
		}
	}

	/**
	 * Writes as much of the pool stack as the slot's limit allows and returns how many were placed
	 * (core's {@code placeStack} without the add-to-existing variant).
	 */
	private static int placeStack(ContainerInventoryHandle handler, ItemStackKey key, int count, int slot) {
		ItemStack stack = key.getStack().copy();
		int newCount = Math.min(count, handler.getSlotLimit(slot));
		stack.setCount(newCount);
		if (!ItemStack.matches(handler.getStackInSlot(slot), stack)) {
			handler.setStackInSlot(slot, stack);
		}
		return newCount;
	}
}