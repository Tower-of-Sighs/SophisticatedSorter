package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.settings.ContainerOpenFlow;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: "close the settings screen and return to the container I was viewing". Sent
 * by the settings screen on ESC and by the return tab. The server reopens the container recorded
 * in {@link com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker} for the player; when
 * the target block entity is gone it falls back to closing the container.
 */
public record ClientCloseContainerSettingsPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ClientCloseContainerSettingsPayload> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "close_container_settings"));

	public static final StreamCodec<ByteBuf, ClientCloseContainerSettingsPayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> {
			},
			buf -> new ClientCloseContainerSettingsPayload());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(ClientCloseContainerSettingsPayload payload, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer serverPlayer) {
				ContainerOpenFlow.closeSettings(serverPlayer);
			}
		});
	}
}
