package com.sighs.sophisticatedsorter.client.settings;

import javax.annotation.Nullable;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;

/**
 * Client-side record of the container the player currently has open. The server resolves the
 * container of each opened menu (its slots reference the real block entity) and pushes the key here
 * via {@link com.sighs.sophisticatedsorter.network.ClientboundTrackedContainerKeyPayload}; the client
 * cannot resolve the key itself because a vanilla menu's client slots wrap a {@code SimpleContainer}
 * that carries no block position.
 */
public final class ClientTrackedContainer {
	@Nullable
	private static ContainerSettingsKey currentKey;

	private ClientTrackedContainer() {
	}

	public static void setCurrentKey(@Nullable ContainerSettingsKey key) {
		currentKey = key;
	}

	@Nullable
	public static ContainerSettingsKey getCurrentKey() {
		return currentKey;
	}
}