package com.sighs.sophisticatedsorter.client.settings;

import com.sighs.sophisticatedsorter.network.ClientCloseContainerSettingsPayload;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ImageButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;

/**
 * The top tab of the container-settings screen that returns the player to the container they were
 * viewing before opening settings. Uses Core's back-icon artwork (the same {@code UV(64, 80)}
 * icon {@code BackToBackpackTab} uses) and, on click, asks the server to close the settings
 * screen and reopen the original container.
 */
public class ContainerReturnTab extends Tab {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 80), Dimension.SQUARE_16);

	public ContainerReturnTab(Position position) {
		super(position, Component.translatable("gui.sophisticatedsorter.return_to_container.tooltip"),
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, ICON, onTabIconClicked));
	}

	@Override
	protected void onTabIconClicked(int button) {
		ClientPacketDistributor.sendToServer(new ClientCloseContainerSettingsPayload());
	}
}