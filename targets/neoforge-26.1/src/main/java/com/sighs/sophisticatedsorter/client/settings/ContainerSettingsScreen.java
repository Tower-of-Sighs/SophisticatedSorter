package com.sighs.sophisticatedsorter.client.settings;

import com.sighs.sophisticatedsorter.network.ClientCloseContainerSettingsPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;

/**
 * The container-settings screen: Sophisticated Core's {@link SettingsScreen} itself with the
 * sorter's own tab control, exactly mirroring how {@code BackpackSettingsScreen} reuses the core
 * screen. ESC is handled by the core screen and routed through
 * {@link #sendStorageInventoryScreenOpenMessage}, which here asks the server to close settings and
 * reopen the container the player came from.
 * <p>
 * 26.1 difference from the 1.21.1 implementation: core removed
 * {@code IItemDisplaySettingsPreviewProvider} (and the {@code getItemDisplaySettingsPreviewProvider}
 * override), so the no-op preview provider class was dropped; the item-display tab is hidden anyway
 * through {@link ContainerSettingsTabControl#isSettingsCategoryDisabled}.
 */
public class ContainerSettingsScreen extends SettingsScreen {
	public ContainerSettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new ContainerSettingsTabControl(this, new Position(this.getLeftX() + this.getImageWidth(), this.getTopY() + 4));
	}

	/** Mirror of {@code BackpackSettingsScreen.constructScreen} used by the menu-screen registration. */
	public static ContainerSettingsScreen constructScreen(SettingsContainerMenu<?> screenContainer, Inventory playerInventory,
			Component title) {
		return new ContainerSettingsScreen(screenContainer, playerInventory, title);
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		ClientPacketDistributor.sendToServer(new ClientCloseContainerSettingsPayload());
	}
}