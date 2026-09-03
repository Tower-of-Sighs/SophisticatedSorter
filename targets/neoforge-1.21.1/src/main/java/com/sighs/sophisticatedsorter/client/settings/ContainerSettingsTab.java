package com.sighs.sophisticatedsorter.client.settings;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ImageButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import com.sighs.sophisticatedsorter.network.ClientOpenContainerSettingsPayload;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;

/**
 * The settings entry shown on qualifying vanilla container screens and on the player-inventory
 * main screen. It is Sophisticated Core's own {@link Tab} - the same tab-with-gear that Core's
 * {@code StorageSettingsTab} renders on storage screens - so the artwork, tab background, hover
 * tooltip and click handling all come from Core.
 * <p>
 * Clicking the gear opens the settings screen for the target the server resolves from the
 * tracker, or for the explicit {@link ContainerSettingsKey} supplied for targets the client knows
 * directly (the player inventory main screen).
 */
public class ContainerSettingsTab extends Tab {
	private static final TextureBlitData GEAR = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(16, 96), Dimension.SQUARE_16);
	@Nullable
	private final ContainerSettingsKey explicitKey;

	public ContainerSettingsTab(Position position) {
		this(position, null);
	}

	/** Creates the tab for a known target (e.g. the player inventory) or an anonymous open request. */
	public ContainerSettingsTab(Position position, @Nullable ContainerSettingsKey explicitKey) {
		super(position, Component.translatable("gui.sophisticatedsorter.settings.open"),
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, GEAR, onTabIconClicked));
		this.explicitKey = explicitKey;
	}

	@Override
	protected void onTabIconClicked(int button) {
		PacketDistributor.sendToServer(explicitKey == null
				? new ClientOpenContainerSettingsPayload()
				: new ClientOpenContainerSettingsPayload(explicitKey));
	}
}
