package com.sighs.sophisticatedsorter.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ServerSortPacket.TYPE, ServerSortPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerTransferPacket.TYPE, ServerTransferPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerSortPacket.TYPE, (payload, context) -> {
            ServerSortPacket.handle(context.player(), payload);
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerTransferPacket.TYPE, (payload, context) -> {
            ServerTransferPacket.handle(context.player(), payload);
        });
    }

    public static void sendToClient(ServerPlayer player, Packet packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToServer(Packet packet) {
        ClientPlayNetworking.send(packet);
    }
}
