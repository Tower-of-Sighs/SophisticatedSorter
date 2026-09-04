package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.Config;
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
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

/**
 * Settings-aware server-side sort for arbitrary containers and the player inventory.
 * <p>
 * When the target of a sort request has a per-container settings record that carries no-sort or
 * memory slot selections, sorting is done <b>in place</b> on the settings wrapper's
 * {@link ContainerInventoryHandler} so that no-sort slots keep whatever they hold and are excluded
 * from compaction.
 * <p>
 * By default (config {@code memorySlotSorting} = true) the sort replicates core's <b>pre-26.1</b>
 * memory-slot semantics ({@code sortIntoMemorizedSlots} from the 1.20.1/1.21.1 core, see
 * {@link #sortMemorizedAware}): memorized slots are emptied first and then refilled, in slot order,
 * with the items whose memory filter they match; everything else fills the remaining slots in the
 * requested order. With {@code memorySlotSorting} = false the sort falls back to 26.1's own sorter
 * ({@link InventorySorter#sortHandler}), which treats memory slots like ordinary slots - memory
 * filtering on vanilla containers is then enforced on placement by {@link ContainerMemorySlotGuard}
 * instead. Sorting runs through the wrapper's handler (a core {@code InventoryHandler}) writing back
 * to the real inventory via the write-through {@code setStackInSlot}.
 * <p>
 * When the target has no settings record, or none of the settings are meaningful for sorting, the
 * method returns {@code false} and the caller falls back to the mod's generic copy-based sort, so
 * plain containers keep their existing behavior.
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
		ServerContainerSettingsStore storage = ServerContainerSettingsStore.get();
		if (storage == null) {
			return false;
		}
		if (key.isPlayerInventory()) {
			ContainerInventoryHandles.PlayerInventoryHandle realInventory =
					new ContainerInventoryHandles.PlayerInventoryHandle(player.getInventory());
			ContainerSettingsWrapper wrapper = ContainerSettingsWrapper.playerInventory(storage, key, realInventory,
					player.level().registryAccess());
			return sortSettingsAware(player, request, wrapper, null);
		}
		ContainerTargetResolver.ContainerTarget target = ContainerTargetResolver.resolveTarget(player, key);
		if (target == null) {
			return false;
		}
		ContainerSettingsWrapper wrapper = new ContainerSettingsWrapper(storage, key, target.slots(), target.itemHandler(),
				target.title(), player.level().registryAccess());
		return sortSettingsAware(player, request, wrapper, target.blockEntity());
	}

	/**
	 * Runs the settings-aware sort when the target's settings are meaningful. A target whose
	 * settings record is completely empty is deliberately left to the generic sort so plain
	 * containers (double chests included) are not disturbed by a half-sorted in-place pass.
	 */
	private static boolean sortSettingsAware(ServerPlayer player, SortRequest request, ContainerSettingsWrapper wrapper,
			@Nullable BlockEntity blockEntity) {
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
		SortExecutionState.withItemMaxStackSizeLimit(() -> {
			if (Config.MEMORY_SLOT_SORTING.get()) {
				// Replicate core's pre-26.1 memory-aware sort: memorized slots are emptied first and then
				// refilled (in slot order) with the items whose filter they match; everything else fills
				// the remaining slots while no-sort slots keep whatever they hold.
				sortMemorizedAware(wrapper.getInventoryHandler(), comparator, noSortSlots, memory, memorySlots);
			} else {
				InventorySorter.sortHandler(wrapper.getInventoryHandler(), comparator, noSortSlots);
			}
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

	/**
	 * In-place sort with the pre-26.1 memory-slot semantics over the settings wrapper's handler (a core
	 * {@link InventoryHandler} whose write-through keeps the real container in sync):
	 * <ol>
	 * <li>every stack outside the no-sort slots is compacted into a type-&gt;count pool;</li>
	 * <li>memorized slots (deduplicated against no-sort) are emptied, then refilled in slot order with
	 * the pool entries whose items match their memory filter, up to the stack limit;</li>
	 * <li>the remaining pool entries are placed back, in sort order, into the free (non-no-sort,
	 * non-memorized) slots, and any leftover slots are emptied.</li>
	 * </ol>
	 * This mirrors {@code InventorySorter.sortIntoMemorizedSlots} + {@code sortIntoOtherSlots} of the
	 * 1.20.1/1.21.1 core, which the 26.1 core removed from its own sorter.
	 */
	private static void sortMemorizedAware(InventoryHandler handler, Comparator<Map.Entry<ItemStackKey, Integer>> comparator,
			Set<Integer> noSortSlots, MemorySettingsCategory memory, Set<Integer> memorySlots) {
		Set<Integer> memorized = new HashSet<>(memorySlots);
		memorized.removeAll(noSortSlots);
		// Compact every slot outside the no-sort slots; memorized slots' current contents join the pool
		// so a non-matching occupant gets re-routed somewhere else during the refill pass.
		Map<ItemStackKey, Integer> compacted = new HashMap<>();
		for (int slot = 0; slot < handler.size(); slot++) {
			if (noSortSlots.contains(slot)) {
				continue;
			}
			ItemStack stack = handler.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				compacted.merge(ItemStackKey.of(stack), stack.getCount(), Integer::sum);
			}
		}
		List<Map.Entry<ItemStackKey, Integer>> entries = new ArrayList<>(compacted.entrySet());
		entries.sort(comparator);
		// Empty the memorized slots, then route the matching pool entries back into them.
		int[] memSlots = memorized.stream().mapToInt(Integer::intValue).sorted().toArray();
		for (int slot : memSlots) {
			handler.setStackInSlot(slot, ItemStack.EMPTY);
		}
		for (Map.Entry<ItemStackKey, Integer> entry : entries) {
			int remaining = entry.getValue();
			if (remaining <= 0) {
				continue;
			}
			for (int slot : memSlots) {
				if (remaining <= 0) {
					break;
				}
				if (memory.matchesFilter(slot, entry.getKey().stack())) {
					int placed = placeStack(handler, entry.getKey(), remaining, slot);
					remaining -= placed;
					entry.setValue(remaining);
				}
			}
		}
		// Refill the free slots in order with what is left; empty everything that gets no item.
		Set<Integer> excluded = new HashSet<>(noSortSlots);
		excluded.addAll(memorized);
		Iterator<Map.Entry<ItemStackKey, Integer>> it = entries.iterator();
		Map.Entry<ItemStackKey, Integer> current = it.hasNext() ? it.next() : null;
		int remaining = current == null ? 0 : current.getValue();
		for (int slot = 0; slot < handler.size(); slot++) {
			if (excluded.contains(slot)) {
				continue;
			}
			// Skip past entries the memory routing already consumed completely.
			while (current != null && remaining <= 0) {
				current = it.hasNext() ? it.next() : null;
				remaining = current == null ? 0 : current.getValue();
			}
			if (current == null) {
				handler.setStackInSlot(slot, ItemStack.EMPTY);
				continue;
			}
			int placed = placeStack(handler, current.getKey(), remaining, slot);
			remaining -= placed;
			if (remaining <= 0) {
				current = it.hasNext() ? it.next() : null;
				remaining = current == null ? 0 : current.getValue();
			}
		}
	}

	/** Vanilla container slot limit for the given item (64, capped by the item's own max stack size). */
	private static int stackLimit(ItemStackKey key) {
		return Math.min(64, key.stack().getMaxStackSize());
	}

	/**
	 * Places up to {@code count} of the key's item into the slot, overwriting a different occupant
	 * (mirrors core's {@code InventorySorter.placeStack}): the pool is authoritative, so a slot that
	 * still holds an item counted earlier is replaced with the stack being placed.
	 */
	private static int placeStack(InventoryHandler handler, ItemStackKey key, int count, int slot) {
		ItemStack stack = key.stack().copy();
		int placed = Math.min(count, stackLimit(key));
		stack.setCount(placed);
		if (!ItemStack.matches(handler.getStackInSlot(slot), stack)) {
			handler.setStackInSlot(slot, stack);
		}
		return placed;
	}
}