package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.Config;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemResourceHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderData;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderDataHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;

/**
 * Functional {@link IStorageWrapper} facade over an arbitrary container's settings. The actual sort
 * operation is deliberately absent - real sorting is wired through core's event/mixin paths in a
 * later stage - so {@link #sort()} only marks the change and refreshes the live slot indexes.
 * <p>
 * 26.1 rewrite of the 1.21.1 wrapper: core's {@code InventoryHandler}/{@code UpgradeHandler} now
 * take a shared {@link ContainerContents} (inventory + partitioner + upgrades + settings data)
 * instead of a raw contents {@code CompoundTag}, and {@code getRenderInfo()} moved to
 * {@link #getRenderDataHandler()} returning a {@link RenderDataHandler}. The settings data is
 * decoded from the store's contents tag (under the {@value ContainerSettingsHandler#SETTINGS_TAG}
 * child, see {@link ContainerSettingsHandler#fromNbt}) with a registry-aware codec, and written
 * back by the dirty handler (see {@link ContainerSettingsHandler#writeToNbt}). Instances are
 * recreated whenever a settings screen opens.
 * <p>
 * The settings storage nbt and the inventory snapshot share one contents tag. The save handler of
 * that tag is also invoked by {@link ContainerInventoryHandler} when the (real) contents of the
 * target change (through its core-driven onContentsChanged), so a file record exists from the
 * moment the sorter actually moves an item.
 * <p>
 * The {@link ContainerSettingsStore} these wrappers are built over is server-authoritative
 * ({@link ServerContainerSettingsStore}, a world SavedData); on the client it is a mirror fed by
 * server pushes ({@code ClientContainerSettingsCache}).
 */
public class ContainerSettingsWrapper implements IStorageWrapper {
	/**
	 * Shared {@link StackUpgradeConfig} hosted in this mod's registered server config (see
	 * {@link com.sighs.sophisticatedsorter.Config.Server}). Core's {@code InventoryHandler} asks its
	 * stack-upgrade config for item stack limits whenever the handler has real slots, and
	 * {@code StackUpgradeConfig.canStackItem} only returns safely when the value it reads belongs to
	 * a registered - and therefore loaded - spec, mirroring how Sophisticated Backpacks hosts its
	 * {@code StackUpgradeConfig} in its server config. A bare {@code new StackUpgradeConfig(new
	 * ModConfigSpec.Builder())} (as core's {@code NoopStorageWrapper} uses) only survives a
	 * zero-slot handler that never asks for a stack limit.
	 */
	private static final StackUpgradeConfig STACK_UPGRADE_CONFIG = Config.SERVER.stackUpgrade;

	private final ContainerSettingsStore storage;
	private final ContainerSettingsKey key;
	private final Runnable contentsSaveHandler;
	private final boolean[] persistentState = new boolean[] {true};
	private final ContainerContents contents;
	private final ContainerInventoryHandler inventoryHandler;
	private final ContainerSettingsHandler settingsHandler;
	private final UpgradeHandler upgradeHandler;
	private final RenderDataHandler renderDataHandler;
	private final Component displayName;
	private final int numberOfSlots;
	private int columnsTaken;
	private SortBy sortBy = SortBy.NAME;

	/**
	 * Creates the wrapper over the block-entity (or otherwise real) item handler of the target.
	 *
	 * @param registries registry access used to (de)serialize the settings data (memory filters hold
	 *                   item references that need a registry-aware codec)
	 */
	public ContainerSettingsWrapper(ContainerSettingsStore storage, ContainerSettingsKey key, int slots,
			@Nullable IItemHandlerModifiable realInventory, Component displayName, HolderLookup.Provider registries) {
		this.storage = storage;
		this.key = key;
		this.displayName = displayName;
		this.numberOfSlots = slots;
		CompoundTag contentsNbt = storage.getOrCreateContents(key);
		this.contentsSaveHandler = () -> storage.markDirty(key);
		// Settings first: Core's InventoryHandler constructor dereferences
		// storageWrapper.getSettingsHandler() (via initSlotTracker on the first getSlotTracker call)
		// while it is still running, so the settings handler must already exist when the inventory
		// handler is built. The suppliers passed here are lazy (this::getInventoryHandler /
		// this::getRenderDataHandler) and are only dereferenced later by the memory / item-display
		// categories, never during construction.
		ContainerContents.SettingsData settingsData = ContainerSettingsHandler.fromNbt(contentsNbt, registries);
		// 26.1 core treats the handler's internal transfer storage as the single source of truth: the
		// settings menu's storage slots read it on every frame (they are ghost slots, never synced
		// per-slot) and core's InventorySorter reads and writes through it as well. The container's
		// items therefore have to be present in the handler from construction. Server-side wrappers
		// snapshot the live real inventory; client-side wrappers (realInventory == null) take the
		// snapshot the server embedded in the opening data under
		// {@value #INVENTORY_SNAPSHOT_TAG} (see {@code ContainerOpenFlow}).
		NonNullList<ItemStack> inventoryStacks = loadInventorySnapshot(contentsNbt, slots, realInventory, registries);
		this.contents = new ContainerContents(new ContainerContents.InventoryData(inventoryStacks), new ContainerContents.PartitionerData(),
				new ContainerContents.UpgradeData(), settingsData);
		this.settingsHandler = new ContainerSettingsHandler(settingsData, () -> {
			if (persistentState[0]) {
				ContainerSettingsHandler.writeToNbt(settingsData, contentsNbt, registries);
				contentsSaveHandler.run();
			}
		}, this::getInventoryHandler, this::getRenderDataHandler);
		this.inventoryHandler = new ContainerInventoryHandler(slots, this, key, realInventory, contents, contentsSaveHandler,
				STACK_UPGRADE_CONFIG);
		// Render data before upgrade handler: Core's UpgradeHandler constructor reads
		// storageWrapper.getRenderDataHandler() while it is still running.
		this.renderDataHandler = new RenderDataHandler(new RenderData(), data -> {
		});
		this.upgradeHandler = new UpgradeHandler(0, this, contents, () -> {
		}, () -> {
		});
	}

	/**
	 * Child tag of the contents record that carries the container's item snapshot for the client
	 * mirror. Written by the server open flow into the menu-opening data (never persisted into the
	 * store itself) and read back by client-side wrappers so the settings screen can display the
	 * container's items.
	 */
	public static final String INVENTORY_SNAPSHOT_TAG = "inventory";

	/**
	 * Builds the initial handler stacks: the live real inventory when one exists (server side),
	 * otherwise the snapshot carried in the contents tag by the opening data (client side).
	 */
	private static NonNullList<ItemStack> loadInventorySnapshot(CompoundTag contentsNbt, int slots,
			@Nullable IItemHandlerModifiable realInventory, HolderLookup.Provider registries) {
		NonNullList<ItemStack> stacks = NonNullList.withSize(slots, ItemStack.EMPTY);
		if (realInventory != null) {
			int count = Math.min(slots, realInventory.getSlots());
			for (int i = 0; i < count; i++) {
				stacks.set(i, realInventory.getStackInSlot(i).copy());
			}
			return stacks;
		}
		ListTag inventoryTag = contentsNbt.getListOrEmpty(INVENTORY_SNAPSHOT_TAG);
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
		int count = Math.min(slots, inventoryTag.size());
		for (int i = 0; i < count; i++) {
			int slot = i;
			ItemStack.OPTIONAL_CODEC.parse(ops, inventoryTag.get(i)).result().ifPresent(stack -> stacks.set(slot, stack));
		}
		return stacks;
	}

	/** Creates the wrapper over the 36 main slots of the player inventory. */
	public static ContainerSettingsWrapper playerInventory(ContainerSettingsStore storage, ContainerSettingsKey key,
			ContainerInventoryHandles.PlayerInventoryHandle realInventory, HolderLookup.Provider registries) {
		return new ContainerSettingsWrapper(storage, key, 36, realInventory, Component.literal("Player Inventory"), registries);
	}

	public ContainerSettingsKey getKey() {
		return key;
	}

	@Override
	public void setContentsChangeHandler(Runnable contentsChangeHandler) {
		// nothing to observe: contents changes are pushed straight through to the storage
	}

	@Override
	public ITrackedContentsItemResourceHandler getInventoryForUpgradeProcessing() {
		return inventoryHandler;
	}

	@Override
	public InventoryHandler getInventoryHandler() {
		return inventoryHandler;
	}

	@Override
	public ITrackedContentsItemResourceHandler getInventoryForInputOutput() {
		return inventoryHandler;
	}

	@Override
	public ContainerSettingsHandler getSettingsHandler() {
		return settingsHandler;
	}

	@Override
	public UpgradeHandler getUpgradeHandler() {
		return upgradeHandler;
	}

	@Override
	public Optional<UUID> getContentsUuid() {
		return Optional.empty();
	}

	@Override
	public int getMainColor() {
		return -1;
	}

	@Override
	public int getAccentColor() {
		return -1;
	}

	@Override
	public void setColors(int mainColor, int accentColor) {
	}

	@Override
	public Optional<Integer> getOpenTabId() {
		return Optional.empty();
	}

	@Override
	public void setOpenTabId(int openTabId) {
		// the settings screen of a later stage manages its own tab state
	}

	@Override
	public void removeOpenTabId() {
	}

	@Override
	public void setSortBy(SortBy sortBy) {
		this.sortBy = sortBy;
	}

	@Override
	public SortBy getSortBy() {
		return sortBy;
	}

	@Override
	public void sort() {
		inventoryHandler.getSlotTracker().refreshSlotIndexesFrom(inventoryHandler);
	}

	@Override
	public void onContentsUpdated() {
	}

	@Override
	public void refreshInventoryForUpgradeProcessing() {
	}

	@Override
	public void refreshInventoryForInputOutput() {
	}

	@Override
	public void setPersistent(boolean persistent) {
		persistentState[0] = persistent;
		inventoryHandler.setPersistent(persistent);
	}

	@Override
	public void fillWithLoot(Player player) {
	}

	@Override
	public RenderDataHandler getRenderDataHandler() {
		return renderDataHandler;
	}

	@Override
	public void setColumnsTaken(int columnsTaken, boolean hasChanged) {
		this.columnsTaken = columnsTaken;
	}

	@Override
	public int getColumnsTaken() {
		return columnsTaken;
	}

	@Override
	public int getNumberOfSlotRows() {
		return (numberOfSlots + 8) / 9;
	}

	@Override
	public String getStorageType() {
		return SophisticatedSorter.MODID + ":container";
	}

	@Override
	public Component getDisplayName() {
		return displayName;
	}
}