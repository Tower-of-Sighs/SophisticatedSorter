package com.sighs.sophisticatedsorter.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.ISettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;

/**
 * {@link SettingsHandler} for arbitrary containers, mirroring how
 * {@code BackpackSettingsHandler} adapts the base handler to its own contents nbt. Unlike
 * backpacks it does not need the wrapper itself: everything it needs is handed in directly.
 * <p>
 * The global category lives under the same core "global" name the core tab/factory map uses, and
 * player-context values are stored under a dedicated player-tag name so they never collide with
 * Sophisticated Core's or Sophisticated Backpacks' player persistent data.
 * <p>
 * The settings nbt is stored inside the passed contents tag under "settings", which keeps it in
 * the same file as any other per-container data this mod writes later.
 */
public class ContainerSettingsHandler extends SettingsHandler {
	public static final String SETTINGS_TAG = "settings";
	public static final String PLAYER_SETTINGS_TAG_NAME = "sophisticatedSorterPlayerSettings";

	public ContainerSettingsHandler(CompoundTag contentsNbt, Runnable markContentsDirty, Supplier<InventoryHandler> inventoryHandlerSupplier,
			Supplier<RenderInfo> renderInfoSupplier) {
		super(contentsNbt, markContentsDirty, inventoryHandlerSupplier, renderInfoSupplier);
	}

	@Override
	protected CompoundTag getSettingsNbtFromContentsNbt(CompoundTag contentsNbt) {
		return contentsNbt.getCompound(SETTINGS_TAG);
	}

	/** The contents tag the settings nbt lives in (same reference the wrapper holds). */
	CompoundTag getContentsNbt() {
		return contentsNbt;
	}

	@Override
	protected void addItemDisplayCategory(Supplier<InventoryHandler> inventoryHandlerSupplier, Supplier<RenderInfo> renderInfoSupplier,
			CompoundTag settingsNbt) {
		// The fabric port of core drops the "can add item display slots" boolean from this
		// constructor (compared with the NeoForge build the reference port calls).
		addSettingsCategory(settingsNbt, ItemDisplaySettingsCategory.NAME, markContentsDirty,
				(categoryNbt, saveNbt) -> new ItemDisplaySettingsCategory(inventoryHandlerSupplier, renderInfoSupplier, categoryNbt, saveNbt, 1,
						() -> getTypeCategory(MemorySettingsCategory.class)));
	}

	@Override
	public String getGlobalSettingsCategoryName() {
		return MainSettingsCategory.NAME;
	}

	@Override
	public ISettingsCategory<?> instantiateGlobalSettingsCategory(CompoundTag categoryNbt, Consumer<CompoundTag> saveNbt) {
		return new MainSettingsCategory<>(categoryNbt, saveNbt, PLAYER_SETTINGS_TAG_NAME);
	}

	@Override
	public MainSettingsCategory<?> getGlobalSettingsCategory() {
		return getTypeCategory(MainSettingsCategory.class);
	}

	@Override
	protected void saveCategoryNbt(CompoundTag settingsNbt, String categoryName, CompoundTag tag) {
		settingsNbt.put(categoryName, tag);
		contentsNbt.put(SETTINGS_TAG, settingsNbt);
		markContentsDirty.run();
	}
}