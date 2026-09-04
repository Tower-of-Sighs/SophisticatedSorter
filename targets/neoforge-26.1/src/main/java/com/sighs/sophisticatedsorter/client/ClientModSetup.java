package com.sighs.sophisticatedsorter.client;

import com.sighs.sophisticatedsorter.client.settings.ContainerSettingsScreen;
import com.sighs.sophisticatedsorter.settings.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-only registration: binds {@link ContainerSettingsScreen} to the container settings menu
 * type. Invoked only when the mod constructs on the client distribution, mirroring how
 * Sophisticated Backpacks registers its settings screen ({@code ModItemsClient}).
 */
public final class ClientModSetup {
	private ClientModSetup() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener(ClientModSetup::onMenuScreenRegister);
	}

	private static void onMenuScreenRegister(RegisterMenuScreensEvent event) {
		event.register(ModMenus.CONTAINER_SETTINGS.get(), ContainerSettingsScreen::constructScreen);
	}
}