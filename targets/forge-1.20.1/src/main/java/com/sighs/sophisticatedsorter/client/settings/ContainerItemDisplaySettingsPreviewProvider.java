package com.sighs.sophisticatedsorter.client.settings;

import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.IItemDisplaySettingsPreviewProvider;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsContainer;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsTab;

/**
 * {@link IItemDisplaySettingsPreviewProvider} for arbitrary containers: there is no in-world item
 * display for them, so the item-display tab's preview renders nothing. All interface methods are
 * no-ops / empty, exactly like the defaults in the interface itself.
 */
public class ContainerItemDisplaySettingsPreviewProvider implements IItemDisplaySettingsPreviewProvider {
	public static final ContainerItemDisplaySettingsPreviewProvider INSTANCE = new ContainerItemDisplaySettingsPreviewProvider();

	private ContainerItemDisplaySettingsPreviewProvider() {
	}

	@Override
	public boolean renderItemDisplaySettingsPreview(ItemDisplaySettingsTab tab, SettingsScreen screen, GuiGraphics guiGraphics, int x, int y,
			int width, int height, ItemDisplaySettingsContainer container, int selectedSlot, float xAxisRotation, float yAxisRotation,
			float partialTicks) {
		// No in-world display target for arbitrary containers - the preview stays empty.
		return false;
	}

	@Override
	public Optional<ItemStack> getItemDisplaySettingsPreviewStack(SettingsScreen screen, ItemDisplaySettingsContainer container, int selectedSlot) {
		return Optional.empty();
	}
}