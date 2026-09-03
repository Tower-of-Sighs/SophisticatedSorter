package com.sighs.sophisticatedsorter.client.settings;

import com.sighs.sophisticatedsorter.network.ClientCloseContainerSettingsPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.settings.StorageSettingsTabControlBase;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.IItemDisplaySettingsPreviewProvider;

/**
 * The container-settings screen: Sophisticated Core's {@link SettingsScreen} itself with the
 * sorter's own tab control and a no-op item-display preview provider, exactly mirroring how
 * {@code BackpackSettingsScreen} reuses the core screen. ESC (key 256) is handled by the core
 * screen and routed through {@link #sendStorageInventoryScreenOpenMessage}, which here asks the
 * server to close settings and reopen the container the player came from.
 */
public class ContainerSettingsScreen extends SettingsScreen {
	public ContainerSettingsScreen(SettingsContainerMenu<?> screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new ContainerSettingsTabControl(this, new Position(this.leftPos + this.imageWidth, this.topPos + 4));
	}

	@Override
	public IItemDisplaySettingsPreviewProvider getItemDisplaySettingsPreviewProvider() {
		return ContainerItemDisplaySettingsPreviewProvider.INSTANCE;
	}

	/** Mirror of {@code BackpackSettingsScreen.constructScreen} used by the menu-screen registration. */
	public static ContainerSettingsScreen constructScreen(SettingsContainerMenu<?> screenContainer, Inventory playerInventory,
			Component title) {
		return new ContainerSettingsScreen(screenContainer, playerInventory, title);
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		PacketDistributor.sendToServer(new ClientCloseContainerSettingsPayload());
	}
}
