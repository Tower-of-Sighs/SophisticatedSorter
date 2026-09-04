package com.sighs.sophisticatedsorter.settings;

import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderDataHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategoryData;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;

/**
 * {@link SettingsHandler} for arbitrary containers, mirroring how
 * {@code BackpackSettingsHandler} adapts the base handler to its own settings data. Unlike
 * backpacks it does not need the wrapper itself: everything it needs is handed in directly.
 * <p>
 * 26.1 rewrite of the 1.21.1 handler: core changed its settings model from a raw {@code CompoundTag}
 * (with an abstract {@code getSettingsNbtFromContentsNbt} seam) to a structured
 * {@link ContainerContents.SettingsData} object shared with the {@code InventoryHandler}. The main
 * ("global"), no-sort and memory categories are now registered by the core base handler itself; this
 * class only has to add the item-display category ("item_display"), exactly like
 * {@code BackpackSettingsHandler}. The player-context values keep the dedicated player-tag name so
 * they never collide with Sophisticated Core's or Sophisticated Backpacks' player persistent data.
 * <p>
 * {@link #fromNbt}/{@link #writeToNbt} (de)serialize the settings data through {@code SettingsData.CODEC}
 * inside a {@link RegistryOps} (memory filters store item references, so a registry-aware ops is
 * required). The persisted tag layout matches the 1.21.1 shape: the store's contents tag keeps the
 * settings under the {@value #SETTINGS_TAG} child.
 */
public class ContainerSettingsHandler extends SettingsHandler {
	public static final String SETTINGS_TAG = "settings";
	public static final String PLAYER_SETTINGS_TAG_NAME = "sophisticatedSorterPlayerSettings";

	public ContainerSettingsHandler(ContainerContents.SettingsData settingsData, Runnable markContentsDirty,
			Supplier<InventoryHandler> inventoryHandlerSupplier, Supplier<RenderDataHandler> renderDataHandlerSupplier) {
		super(settingsData, markContentsDirty, inventoryHandlerSupplier, renderDataHandlerSupplier, PLAYER_SETTINGS_TAG_NAME);
	}

	@Override
	protected void addItemDisplayCategory(Supplier<InventoryHandler> inventoryHandlerSupplier,
			Supplier<RenderDataHandler> renderDataHandlerSupplier, ContainerContents.SettingsData settingsData) {
		addSettingsCategory(settingsData, ItemDisplaySettingsCategory.NAME, markContentsDirty,
				(categoryData, saveNbt) -> new ItemDisplaySettingsCategory(inventoryHandlerSupplier, renderDataHandlerSupplier,
						categoryData, saveNbt, 1, () -> getTypeCategory(MemorySettingsCategory.class)),
				ItemDisplaySettingsCategoryData::new);
	}

	/** Decodes the settings section of a contents tag; an empty/absent section yields fresh defaults. */
	public static ContainerContents.SettingsData fromNbt(CompoundTag contentsNbt, HolderLookup.Provider registries) {
		CompoundTag settingsNbt = contentsNbt.getCompoundOrEmpty(SETTINGS_TAG);
		if (settingsNbt.isEmpty()) {
			return new ContainerContents.SettingsData();
		}
		return ContainerContents.SettingsData.CODEC
				.parse(RegistryOps.create(NbtOps.INSTANCE, registries), settingsNbt)
				.result().orElseGet(ContainerContents.SettingsData::new);
	}

	/** Encodes the settings data into the settings section of a contents tag. */
	public static void writeToNbt(ContainerContents.SettingsData settingsData, CompoundTag contentsNbt,
			HolderLookup.Provider registries) {
		CompoundTag settingsNbt = ContainerContents.SettingsData.CODEC
				.encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), settingsData)
				.result()
				.flatMap(tag -> tag instanceof CompoundTag compound ? java.util.Optional.of(compound) : java.util.Optional.empty())
				.orElseGet(CompoundTag::new);
		contentsNbt.put(SETTINGS_TAG, settingsNbt);
	}
}