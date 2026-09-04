package com.sighs.sophisticatedsorter.client.settings;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsContainerBase;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsTab;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsTab;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsTab;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsTab;

/**
 * Tab control for the container-settings screen. It maps the container settings categories
 * ("global", "no_sort", "memory", "item_display") that {@code ContainerSettingsHandler} registers
 * to Sophisticated Core's own settings tabs - {@link net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsTab}
 * (constructed through {@link ContainerMainSettingsTab}), {@link NoSortSettingsTab},
 * {@link MemorySettingsTab} and {@link ItemDisplaySettingsTab} - plus the
 * {@link ContainerReturnTab} on top, mirroring how {@code BackpackSettingsTabControl} maps
 * backpacks' categories to the core tabs.
 */
public class ContainerSettingsTabControl extends StorageSettingsTabControlBase {
	private static final Map<String, ISettingsTabFactory<?, ?>> SETTINGS_TAB_FACTORIES;

	public ContainerSettingsTabControl(SettingsScreen screen, Position position) {
		super(screen, position);
	}

	@Override
	protected Tab instantiateReturnBackTab() {
		return new ContainerReturnTab(new Position(this.x, this.getTopY()));
	}

	@Override
	protected boolean isSettingsCategoryDisabled(String categoryName) {
		// "global" (main) and "item_display" tabs are not used for arbitrary containers yet - the
		// container-specific settings that matter are no-sort and memory. Keeping the categories
		// registered (their data still round-trips to the server store) but hiding the tabs.
		return "global".equals(categoryName) || "item_display".equals(categoryName)
				|| super.isSettingsCategoryDisabled(categoryName);
	}

	@Override
	protected <C extends SettingsContainerBase<?>, T extends SettingsTab<C>> ISettingsTabFactory<C, T> getSettingsTabFactory(String name) {
		return (ISettingsTabFactory<C, T>) SETTINGS_TAB_FACTORIES.get(name);
	}

	static {
		ImmutableMap.Builder<String, ISettingsTabFactory<?, ?>> builder = ImmutableMap.builder();
		addFactory(builder, "global", ContainerMainSettingsTab::new);
		addFactory(builder, "no_sort", NoSortSettingsTab::new);
		addFactory(builder, "memory", MemorySettingsTab::new);
		addFactory(builder, "item_display", ItemDisplaySettingsTab::new);
		SETTINGS_TAB_FACTORIES = builder.build();
	}
}