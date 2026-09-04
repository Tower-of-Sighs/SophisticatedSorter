package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.common.SortExecutionState;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SortTarget;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

/**
 * Settings-aware server-side sort for arbitrary containers and the player inventory.
 * <p>
 * When the target of a sort request has a per-container settings record that carries no-sort or
 * memory slot selections, sorting is done <b>in place</b> with core's own settings-aware sorter
 * ({@link InventorySorter#sortHandler}) so that:
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
 * runs over the target's <i>real</i> item handler / the player's real {@link Inventory}, not over
 * the wrapper's {@link ContainerInventoryHandler}. Core's {@link InventorySorter} detects a core
 * {@code InventoryHandler} and then writes through {@code getSlotStack}/{@code setSlotStack}/
 * {@code getBaseStackLimit} - the private tracking list - and never invokes the write-through
 * {@code syncToRealInventory} half of {@link ContainerInventoryHandler}, so sorting through the
 * wrapper would leave its tracking copy and the real inventory out of sync (and the tracking nbt
 * would later be written back over the sorted real contents). The real handlers are plain
 * {@link IItemHandlerModifiable}, so the sorter takes the direct slot-read/write path against the
 * single canonical source of truth. The settings file only ever stores settings nbt; container
 * contents are saved through the block entity's own save path.
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
			IItemHandlerModifiable sortTarget, @javax.annotation.Nullable BlockEntity blockEntity) {
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
		SortExecutionState.withItemMaxStackSizeLimit(() -> InventorySorter.sortHandler(sortTarget, comparator, noSortSlots, memorySlots,
				memory::matchesFilter));
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
}