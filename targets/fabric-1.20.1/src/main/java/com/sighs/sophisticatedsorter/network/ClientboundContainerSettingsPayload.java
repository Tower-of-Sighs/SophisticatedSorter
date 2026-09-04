package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server -&gt; client: the authoritative settings contents of one container. Sent when a container is
 * opened (so highlights/ghosts have data) and whenever its settings change (so the open settings
 * screen and the normal container screen stay in sync). A null contents clears the client's mirror
 * for that key.
 */
public class ClientboundContainerSettingsPayload implements Packet {
	private final ContainerSettingsKey key;
	private final CompoundTag contents;

	public ClientboundContainerSettingsPayload(ContainerSettingsKey key, CompoundTag contents) {
		this.key = key;
		this.contents = contents;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		key.write(buf);
		buf.writeNbt(contents);
	}

	public static ClientboundContainerSettingsPayload decode(FriendlyByteBuf buf) {
		return new ClientboundContainerSettingsPayload(ContainerSettingsKey.fromBuffer(buf), buf.readNbt());
	}

	public void handle() {
		if (contents == null) {
			ClientContainerSettingsCache.remove(key);
		} else {
			ClientContainerSettingsCache.putContents(key, contents);
		}
	}
}