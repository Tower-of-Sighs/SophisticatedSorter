package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.p3pp3rf1y.sophisticatedcore.util.StreamCodecHelper;

/**
 * Server -&gt; client: the authoritative settings contents of one container. Sent when a container is
 * opened (so highlights/ghosts have data) and whenever its settings change (so the open settings
 * screen and the normal container screen stay in sync). A null contents clears the client's mirror
 * for that key.
 */
public record ClientboundContainerSettingsPayload(ContainerSettingsKey key, CompoundTag contents)
		implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ClientboundContainerSettingsPayload> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "container_settings_contents"));

	public static final StreamCodec<ByteBuf, ClientboundContainerSettingsPayload> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ClientboundContainerSettingsPayload decode(ByteBuf buf) {
			FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
			ContainerSettingsKey key = ContainerSettingsKey.fromBuffer(friendlyBuf);
			CompoundTag contents = StreamCodecHelper.ofNullable(ByteBufCodecs.COMPOUND_TAG).decode(friendlyBuf);
			return new ClientboundContainerSettingsPayload(key, contents);
		}

		@Override
		public void encode(ByteBuf buf, ClientboundContainerSettingsPayload payload) {
			FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
			payload.key().write(friendlyBuf);
			StreamCodecHelper.ofNullable(ByteBufCodecs.COMPOUND_TAG).encode(friendlyBuf, payload.contents());
		}
	};

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(ClientboundContainerSettingsPayload payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			if (payload.contents() == null) {
				ClientContainerSettingsCache.remove(payload.key());
			} else {
				ClientContainerSettingsCache.putContents(payload.key(), payload.contents());
			}
		});
	}
}