package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKeyCodec;
import com.sighs.sophisticatedsorter.settings.ContainerOpenFlow;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client -> server: "open the container-settings screen for the container the player currently
 * has open". The payload is anonymous on purpose - the gear entry on a vanilla container screen
 * cannot know its own menu's dimension/position, so the server resolves the target from
 * {@link ContainerSettingsTracker}. An optional explicit key is accepted for callers that know
 * their target (and for the player-inventory case).
 */
public class ClientOpenContainerSettingsPayload implements Packet {
	private final ContainerSettingsKey key;

	public ClientOpenContainerSettingsPayload(ContainerSettingsKey key) {
		this.key = key;
	}

	public ClientOpenContainerSettingsPayload() {
		this(null);
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		ContainerSettingsKeyCodec.writeNullable(key, buf);
	}

	public static ClientOpenContainerSettingsPayload decode(FriendlyByteBuf buf) {
		return new ClientOpenContainerSettingsPayload(ContainerSettingsKeyCodec.readNullable(buf));
	}

	public void handle(ServerPlayer player) {
		if (player == null) {
			return;
		}
		ContainerSettingsKey resolvedKey = key != null ? key : ContainerSettingsTracker.get().getOpenKey(player);
		if (resolvedKey != null) {
			ContainerOpenFlow.openSettings(player, resolvedKey);
		}
	}
}