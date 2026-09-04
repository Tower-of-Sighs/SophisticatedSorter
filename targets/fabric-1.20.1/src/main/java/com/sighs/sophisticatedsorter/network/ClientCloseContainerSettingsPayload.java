package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.settings.ContainerOpenFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client -> server: "close the settings screen and return to the container I was viewing". Sent
 * by the settings screen on ESC and by the return tab. The server reopens the container recorded
 * in {@link com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker} for the player; when
 * the target block entity is gone it falls back to closing the container.
 */
public class ClientCloseContainerSettingsPayload implements Packet {
	public ClientCloseContainerSettingsPayload() {
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
	}

	public static ClientCloseContainerSettingsPayload decode(FriendlyByteBuf buf) {
		return new ClientCloseContainerSettingsPayload();
	}

	public void handle(ServerPlayer player) {
		if (player != null) {
			ContainerOpenFlow.closeSettings(player);
		}
	}
}