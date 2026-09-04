package com.sighs.sophisticatedsorter.client;

import com.sighs.sophisticatedsorter.client.settings.ContainerSettingsScreen;
import com.sighs.sophisticatedsorter.settings.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Client-only registration for stage 2: binds {@link ContainerSettingsScreen} to the container
 * settings menu type. Invoked from the client entry point, mirroring how Sophisticated Backpacks
 * registers its settings screen ({@code ModItemsClient}) - on Fabric the menu screen is bound
 * through vanilla {@link MenuScreens#register} (the fabric screen-handler API routes extended
 * menus through the same registry).
 */
public final class ClientModSetup {
	private ClientModSetup() {
	}

	public static void init() {
		MenuScreens.register(ModMenus.CONTAINER_SETTINGS, ContainerSettingsScreen::constructScreen);
	}
}