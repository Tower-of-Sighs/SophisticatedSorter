package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.client.settings.ClientTrackedContainer;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKeyCodec;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server -&gt; client: tells the client which container the player just opened (resolved server-side,
 * where menu slots reference the real block entity). The client stores it so vanilla container
 * screens can render the per-slot settings highlights; a null key clears the record (menu closed or
 * not a supported container).
 */
public class ClientboundTrackedContainerKeyPayload implements Packet {
	private final ContainerSettingsKey key;

	public ClientboundTrackedContainerKeyPayload(ContainerSettingsKey key) {
		this.key = key;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		ContainerSettingsKeyCodec.writeNullable(key, buf);
	}

	public static ClientboundTrackedContainerKeyPayload decode(FriendlyByteBuf buf) {
		return new ClientboundTrackedContainerKeyPayload(ContainerSettingsKeyCodec.readNullable(buf));
	}

	public void handle() {
		ClientTrackedContainer.setCurrentKey(key);
	}
}