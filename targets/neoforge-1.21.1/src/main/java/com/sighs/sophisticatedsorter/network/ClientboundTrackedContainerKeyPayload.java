package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.client.settings.ClientTrackedContainer;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKeyCodec;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -&gt; client: tells the client which container the player just opened (resolved server-side,
 * where menu slots reference the real block entity). The client stores it so vanilla container
 * screens can render the per-slot settings highlights; a null key clears the record (menu closed or
 * not a supported container).
 */
public record ClientboundTrackedContainerKeyPayload(@Nullable ContainerSettingsKey key) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ClientboundTrackedContainerKeyPayload> TYPE = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "tracked_container_key"));

	public static final StreamCodec<ByteBuf, ClientboundTrackedContainerKeyPayload> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ClientboundTrackedContainerKeyPayload decode(ByteBuf buf) {
			net.minecraft.network.FriendlyByteBuf friendlyBuf = new net.minecraft.network.FriendlyByteBuf(buf);
			return new ClientboundTrackedContainerKeyPayload(ContainerSettingsKeyCodec.readNullable(friendlyBuf));
		}

		@Override
		public void encode(ByteBuf buf, ClientboundTrackedContainerKeyPayload payload) {
			net.minecraft.network.FriendlyByteBuf friendlyBuf = new net.minecraft.network.FriendlyByteBuf(buf);
			ContainerSettingsKeyCodec.writeNullable(payload.key(), friendlyBuf);
		}
	};

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(ClientboundTrackedContainerKeyPayload payload, IPayloadContext context) {
		context.enqueueWork(() -> ClientTrackedContainer.setCurrentKey(payload.key()));
	}
}
