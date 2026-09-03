package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKeyCodec;
import com.sighs.sophisticatedsorter.settings.ContainerOpenFlow;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: "open the container-settings screen for the container the player currently
 * has open". The payload is anonymous on purpose - the gear entry on a vanilla container screen
 * cannot know its own menu's dimension/position, so the server resolves the target from
 * {@link ContainerSettingsTracker}. An optional explicit key is accepted for callers that know
 * their target (and for the player-inventory case).
 */
public record ClientOpenContainerSettingsPayload(@Nullable ContainerSettingsKey key) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ClientOpenContainerSettingsPayload> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "open_container_settings"));

	public static final StreamCodec<ByteBuf, ClientOpenContainerSettingsPayload> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ClientOpenContainerSettingsPayload decode(ByteBuf buf) {
			net.minecraft.network.FriendlyByteBuf friendlyBuf = new net.minecraft.network.FriendlyByteBuf(buf);
			ContainerSettingsKey key = ContainerSettingsKeyCodec.readNullable(friendlyBuf);
			return new ClientOpenContainerSettingsPayload(key);
		}

		@Override
		public void encode(ByteBuf buf, ClientOpenContainerSettingsPayload payload) {
			net.minecraft.network.FriendlyByteBuf friendlyBuf = new net.minecraft.network.FriendlyByteBuf(buf);
			ContainerSettingsKeyCodec.writeNullable(payload.key(), friendlyBuf);
		}
	};

	public ClientOpenContainerSettingsPayload() {
		this((ContainerSettingsKey) null);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(ClientOpenContainerSettingsPayload payload, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer serverPlayer)) {
				return;
			}
			ContainerSettingsKey key = payload.key() != null ? payload.key() : ContainerSettingsTracker.get().getOpenKey(serverPlayer);
			if (key != null) {
				ContainerOpenFlow.openSettings(serverPlayer, key);
			}
		});
	}
}
