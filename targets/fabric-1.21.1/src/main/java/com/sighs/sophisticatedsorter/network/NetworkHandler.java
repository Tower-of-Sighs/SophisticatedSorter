package com.sighs.sophisticatedsorter.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Fabric 1.21.1 typed-payload registration and client-to-server transport. */
public final class NetworkHandler {
    private NetworkHandler() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ServerSortPacket.TYPE, ServerSortPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerTransferPacket.TYPE, ServerTransferPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerSortPacket.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ServerTransferPacket.TYPE,
                (payload, context) -> context.server().execute(() -> payload.handle(context.player())));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
