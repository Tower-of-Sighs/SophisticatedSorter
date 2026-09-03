package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.Config;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;

/**
 * Functional {@link IStorageWrapper} facade over an arbitrary container's settings. The actual sort
 * operation is deliberately absent - real sorting is wired through core's event/mixin paths in a
 * later stage - so {@link #sort()} only marks the change and refreshes the live slot indexes.
 * <p>
 * The settings storage nbt and the inventory snapshot share one contents tag. The save handler of
 * that tag is also invoked by {@link ContainerInventoryHandler} when the (real) contents of the
 * target change, so a file record is created as soon as the sorter actually moves an item.
 * <p>
 * Instances are recreated whenever a settings screen opens. Only the shared
 * {@link ContainerSettingsStorage} keeps state across those opens.
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

	private final ContainerSettingsStorage storage;
	private final ContainerSettingsKey key;
	private final Runnable contentsSaveHandler;
	private final boolean[] persistentState = new boolean[] {true};
	private final ContainerInventoryHandler inventoryHandler;
	private final ContainerSettingsHandler settingsHandler;
	private final UpgradeHandler upgradeHandler;
	private final RenderInfo renderInfo;
	private final Component displayName;
	private final int numberOfSlots;
	private int columnsTaken;
	private SortBy sortBy = SortBy.NAME;

	/** Creates the wrapper over the block-entity (or otherwise real) item handler of the target. */
	public ContainerSettingsWrapper(ContainerSettingsStorage storage, ContainerSettingsKey key, int slots,
			@Nullable IItemHandlerModifiable realInventory, Component displayName) {
		this.storage = storage;
		this.key = key;
		this.displayName = displayName;
		this.numberOfSlots = slots;
		CompoundTag contentsNbt = storage.getOrCreateContents(key);
		this.contentsSaveHandler = () -> storage.markDirty(key);
		// Settings first: Core's InventoryHandler constructor dereferences
		// storageWrapper.getSettingsHandler() (via initSlotTracker) while it is still running, so the
		// settings handler must already exist when the inventory handler is built. The supplier passed
		// here is lazy (this::getInventoryHandler) and is only dereferenced later by the memory /
		// item-display categories, never during construction.
		this.settingsHandler = new ContainerSettingsHandler(contentsNbt, () -> {
			if (persistentState[0]) {
				contentsSaveHandler.run();
			}
		}, this::getInventoryHandler, this::getRenderInfo);
		this.inventoryHandler = new ContainerInventoryHandler(slots, this, key, realInventory, contentsNbt, contentsSaveHandler,
				STACK_UPGRADE_CONFIG);
		// Render info before upgrade handler: Core's UpgradeHandler constructor reads
		// storageWrapper.getRenderInfo() while it is still running.
		this.renderInfo = new ContainerSettingsRenderInfo();
		this.upgradeHandler = new UpgradeHandler(0, this, new CompoundTag(), () -> {}, () -> {});
	}

	/** Creates the wrapper over the 36 main slots of the player inventory. */
	public static ContainerSettingsWrapper playerInventory(ContainerSettingsStorage storage, ContainerSettingsKey key,
			ContainerInventoryHandles.PlayerInventoryHandle realInventory) {
		return new ContainerSettingsWrapper(storage, key, 36, realInventory, Component.literal("Player Inventory"));
	}

	public ContainerSettingsKey getKey() {
		return key;
	}

	/** Live settings nbt (the "settings" section of the contents this wrapper was built from). */
	public CompoundTag getSettingsNbt() {
		return settingsHandler.getNbt();
	}

	/**
	 * Live contents nbt this wrapper reads from and writes to. The settings categories and the
	 * inventory snapshot both live in this single tag; mutating it directly keeps the storage and
	 * the handlers coherent.
	 */
	public CompoundTag getContentsNbt() {
		return settingsHandler.getContentsNbt();
	}

	@Override
	public void setContentsChangeHandler(Runnable contentsChangeHandler) {
		// nothing to observe: contents changes are pushed straight through to the storage
	}

	@Override
	public ITrackedContentsItemHandler getInventoryForUpgradeProcessing() {
		return inventoryHandler;
	}

	@Override
	public InventoryHandler getInventoryHandler() {
		return inventoryHandler;
	}

	@Override
	public ITrackedContentsItemHandler getInventoryForInputOutput() {
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
	public void setColors(int mainColor, int accentColor) {
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
	public void onContentsNbtUpdated() {
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
	public RenderInfo getRenderInfo() {
		return renderInfo;
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
