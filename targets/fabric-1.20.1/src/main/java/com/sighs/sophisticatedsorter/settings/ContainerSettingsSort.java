package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.common.SortExecutionState;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SortStackLimitPolicy;
import com.sighs.sophisticatedsorter.common.SortTarget;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsCategory;

/**
 * Settings-aware server-side sort for arbitrary containers and the player inventory.
 * <p>
 * When the target of a sort request has a per-container settings record that carries no-sort or
 * memory slot selections, sorting is done <b>in place</b> with a settings-aware sort so that:
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
 * runs over the target's <i>real</i> item storage / the player's real {@link Inventory}, not over
 * the wrapper's {@link ContainerInventoryHandler}. Core's sorter detects a core {@code InventoryHandler}
 * and then writes through its private tracking list - never invoking the write-through
 * {@code syncToRealInventory} half of {@link ContainerInventoryHandler} - so sorting through the
 * wrapper would leave its tracking copy and the real inventory out of sync (and the tracking nbt
 * would later be written back over the sorted real contents). The real storage is a plain
 * {@link SlottedStackStorage}, so the sorter takes the direct slot-read/write path against the
 * single canonical source of truth. The settings file only ever stores settings nbt; container
 * contents are saved through the block entity's own save path.
 * <p>
 * <b>Version adaptation:</b> the 1.21.1 reference delegates the whole pass to core's five-argument
 * {@code InventorySorter.sortHandler(handler, comparator, noSortSlots, memorySlots, filter)}; core
 * 1.20.1 only ships the three-argument no-sort variant, so the memory-slot pass is implemented here
 * with the same semantics as the reference (memory slots emptied in slot order, then refilled - empty
 * slots only - by items whose filter matches, before the remaining items are placed front-to-back
 * into the other sortable slots).
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
		ContainerSettingsWrapper wrapper = new ContainerSettingsWrapper(storage, key, target.slots(), target.storage(),
				target.title());
		return sortSettingsAware(player, request, wrapper, target.storage(), target.blockEntity());
	}

	/**
	 * Runs the settings-aware sort when the target's settings are meaningful. A target whose
	 * settings record is completely empty is deliberately left to the generic sort so plain
	 * containers (double chests included) are not disturbed by a half-sorted in-place pass.
	 */
	private static boolean sortSettingsAware(ServerPlayer player, SortRequest request, ContainerSettingsWrapper wrapper,
			SlottedStackStorage sortTarget, BlockEntity blockEntity) {
		Set<Integer> noSortSlots = wrapper.getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class).getNoSortSlots();
		MemorySettingsCategory memory = wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class);
		Set<Integer> memorySlots = memory.getSlotIndexes();
		if (noSortSlots.isEmpty() && memorySlots.isEmpty()) {
			return false;
		}
		// The mod's sort criterion travels from the client; wrapper.getSortBy() is the container
		// settings' own preference and stays at its NAME default for these targets. Both are resolved
		// through the same comparator selection core's own menus use.
		Comparator<Map.Entry<ItemStackKey, Integer>> comparator = CoreUtils.getComparator(wrapper.getSortBy(), request.pinyinOrder());
		SortExecutionState.withItemMaxStackSizeLimit(() -> sortSettingsAware(sortTarget, comparator, noSortSlots, memorySlots, memory));
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

	/**
	 * The settings-aware in-place pass over a real {@link SlottedStackStorage}: memory slots emptied
	 * first, then filled (empty slots only, in slot order) with items matching the slot's filter, and
	 * the remaining pool placed front-to-back into the other sortable slots. Leftovers that cannot fit
	 * are stacked back onto no-sort slots that already hold the same item, mirroring core's own
	 * leftover handling.
	 */
	private static void sortSettingsAware(SlottedStackStorage storage, Comparator<Map.Entry<ItemStackKey, Integer>> comparator,
			Set<Integer> noSortSlots, Set<Integer> memorySlots, MemorySettingsCategory memory) {
		// Pool the contents of every non-no-sort slot (memory slots included - their items join the
		// pool so they are redistributed through their filters).
		Map<ItemStackKey, Integer> pooled = new HashMap<>();
		for (int slot = 0; slot < storage.getSlotCount(); slot++) {
			if (noSortSlots.contains(slot)) {
				continue;
			}
			ItemStack stack = storage.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				pooled.merge(ItemStackKey.of(stack), stack.getCount(), Integer::sum);
			}
		}
		List<Map.Entry<ItemStackKey, Integer>> entries = new ArrayList<>(pooled.entrySet());
		entries.sort(comparator);
		List<Integer> memorySlotsSorted = memorySlots.stream().sorted().toList();
		// Empty the memory slots before anything is placed.
		for (int slot : memorySlotsSorted) {
			storage.setStackInSlot(slot, ItemStack.EMPTY);
		}
		// Memory slots first: empty slots only, filter-matched, in slot order (mirrors the reference's
		// sortIntoMemorizedSlots, where the filter predicate is the per-slot matchesFilter).
		Iterator<Map.Entry<ItemStackKey, Integer>> iterator = entries.iterator();
		while (iterator.hasNext()) {
			Map.Entry<ItemStackKey, Integer> entry = iterator.next();
			int remaining = entry.getValue();
			for (int slot : memorySlotsSorted) {
				if (!storage.getStackInSlot(slot).isEmpty()) {
					continue;
				}
				if (!memory.matchesFilter(slot, entry.getKey().getStack())) {
					continue;
				}
				remaining -= placeStack(storage, entry.getKey(), remaining, slot, false);
				if (remaining <= 0) {
					iterator.remove();
					break;
				}
			}
			if (remaining > 0) {
				entry.setValue(remaining);
			}
		}
		// Remaining items into the other (non-memory, non-no-sort) slots, front to back.
		List<Map.Entry<ItemStackKey, Integer>> leftovers = sortIntoOtherSlots(storage, noSortSlots, entries, storage.getSlotCount());
		// Leftovers that still do not fit go back onto no-sort slots that already hold the same item.
		if (!leftovers.isEmpty() && !noSortSlots.isEmpty()) {
			sortIntoNoSortSlots(storage, noSortSlots, leftovers);
		}
	}

	/**
	 * Places the pool entries front-to-back into every slot that is neither a no-sort nor handled by
	 * the memory pass, clearing any leftover slot once the pool is exhausted. Returns the entries
	 * that could not be fully placed.
	 */
	private static List<Map.Entry<ItemStackKey, Integer>> sortIntoOtherSlots(SlottedStackStorage storage, Set<Integer> noSortSlots,
			List<Map.Entry<ItemStackKey, Integer>> entries, int slotCount) {
		List<Map.Entry<ItemStackKey, Integer>> leftovers = new ArrayList<>();
		Iterator<Map.Entry<ItemStackKey, Integer>> iterator = entries.iterator();
		Map.Entry<ItemStackKey, Integer> current = null;
		int count = 0;
		for (int slot = 0; slot < slotCount; slot++) {
			if (noSortSlots.contains(slot)) {
				continue;
			}
			if (current == null || count <= 0) {
				if (!iterator.hasNext()) {
					// Nothing left to place: the slot starts empty after the compaction, clear any residue.
					storage.setStackInSlot(slot, ItemStack.EMPTY);
					continue;
				}
				current = iterator.next();
				count = current.getValue();
			}
			count -= placeStack(storage, current.getKey(), count, slot, false);
			if (count <= 0) {
				current = null;
			}
		}
		if (current != null && count > 0) {
			current.setValue(count);
			leftovers.add(current);
		}
		return leftovers;
	}

	/** Stacks leftover items onto no-sort slots that already hold the same item (mirrors core's leftover pass). */
	private static void sortIntoNoSortSlots(SlottedStackStorage storage, Set<Integer> noSortSlots,
			List<Map.Entry<ItemStackKey, Integer>> leftovers) {
		for (Map.Entry<ItemStackKey, Integer> leftover : leftovers) {
			int remaining = leftover.getValue();
			for (int slot : noSortSlots) {
				if (remaining <= 0) {
					break;
				}
				if (!ItemStack.isSameItemSameTags(storage.getStackInSlot(slot), leftover.getKey().getStack())) {
					continue;
				}
				remaining -= placeStack(storage, leftover.getKey(), remaining, slot, true);
			}
			if (remaining > 0) {
				leftover.setValue(remaining);
			}
		}
	}

	/**
	 * Places up to {@code count} items of the key's stack into the slot and returns the placed
	 * amount. Mirrors core's private {@code placeStack} (including its stack-limit handling through
	 * the mod's {@link SortStackLimitPolicy}, which the shared sorter mixin applies to core's own
	 * placements).
	 */
	private static int placeStack(SlottedStackStorage storage, ItemStackKey key, int count, int slot, boolean checkIfFull) {
		ItemStack prototype = key.getStack().copy();
		int limit = SortStackLimitPolicy.apply(storage.getSlotLimit(slot), prototype.getMaxStackSize(),
				SortExecutionState.shouldLimitToItemMaxStackSize());
		ItemStack current = storage.getStackInSlot(slot);
		int existing = current.getCount();
		int total = Math.min(checkIfFull ? count + existing : count, limit);
		prototype.setCount(total);
		if (!ItemStack.isSameItemSameTags(current, prototype) || existing != total) {
			storage.setStackInSlot(slot, prototype);
		}
		return checkIfFull ? Math.max(total - existing, 0) : total;
	}
}