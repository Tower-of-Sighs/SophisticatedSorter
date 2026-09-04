package com.sighs.sophisticatedsorter.client.settings;

import com.sighs.sophisticatedsorter.network.ClientCloseContainerSettingsPayload;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;

/**
 * The container-settings screen: Sophisticated Core's {@link SettingsScreen} itself with the
 * sorter's own tab control, exactly mirroring how {@code BackpackSettingsScreen} reuses the core
 * screen. ESC (key 256) is handled by the core screen and routed through
 * {@link #sendStorageInventoryScreenOpenMessage}, which here asks the server to close settings and
 * reopen the container the player came from.
 * <p>
 * Note: the 1.21.1 reference overrides {@code getItemDisplaySettingsPreviewProvider} with a no-op
 * preview provider for the item-display tab. Core 1.20.1 predates that hook
 * ({@code IItemDisplaySettingsPreviewProvider} does not exist on this version), and the
 * item-display tab is disabled for these targets anyway, so the override is omitted here - there is
 * no in-world display for arbitrary containers on either version.
 */
public class ContainerSettingsScreen extends SettingsScreen {
	public ContainerSettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new ContainerSettingsTabControl(this, new Position(this.leftPos + this.imageWidth, this.topPos + 4));
	}

	/** Mirror of {@code BackpackSettingsScreen.constructScreen} used by the menu-screen registration. */
	public static ContainerSettingsScreen constructScreen(SettingsContainerMenu<?> screenContainer, Inventory playerInventory,
			Component title) {
		return new ContainerSettingsScreen(screenContainer, playerInventory, title);
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		NetworkHandler.sendToServer(NetworkHandler.CLOSE_CONTAINER_SETTINGS_ID, new ClientCloseContainerSettingsPayload());
	}
}