package com.sighs.sophisticatedsorter.client.settings;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ImageButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsContainer;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsTab;

/**
 * Adapts Core's {@link MainSettingsTab} to the generic {@link MainSettingsContainer} that
 * {@code ContainerSettingsHandler} instantiates for the "global" category, mirroring how
 * Sophisticated Backpacks subclasses the core tab for its own container. The container has no
 * storage context of its own, so the context toggle only offers the generic player wording and
 * the icon is the standard settings gear from the icons sheet.
 */
public class ContainerMainSettingsTab extends MainSettingsTab<MainSettingsContainer> {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(16, 96), Dimension.SQUARE_16);
	private static final List<Component> EMPTY_CONTEXT_TOOLTIP = List.of(
			Component.translatable("gui.sophisticatedsorter.settings.context.tooltip")
					.withStyle(ChatFormatting.GRAY));

	public ContainerMainSettingsTab(MainSettingsContainer container, Position position, SettingsScreen screen) {
		super(container, position, screen, EMPTY_CONTEXT_TOOLTIP, Component.translatable("gui.sophisticatedsorter.settings.context"),
				"gui.sophisticatedsorter.settings.main", "gui.sophisticatedsorter.settings.main.tooltip",
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, ICON, onTabIconClicked));
	}
}
