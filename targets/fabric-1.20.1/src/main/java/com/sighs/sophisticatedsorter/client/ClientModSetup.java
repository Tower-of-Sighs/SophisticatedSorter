package com.sighs.sophisticatedsorter.client;

import com.sighs.sophisticatedsorter.client.settings.ContainerSettingsScreen;
import com.sighs.sophisticatedsorter.settings.ModMenus;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

/**
 * Client-only registration for the container-settings stage: binds {@link ContainerSettingsScreen}
 * to the container settings menu type. Invoked only from the client initializer, mirroring how
 * Sophisticated Backpacks registers its settings screen ({@code ModItemsClient}).
 */
public final class ClientModSetup {
	private ClientModSetup() {
	}

	public static void init() {
		ScreenRegistry.register(ModMenus.CONTAINER_SETTINGS, ContainerSettingsScreen::constructScreen);
	}
}