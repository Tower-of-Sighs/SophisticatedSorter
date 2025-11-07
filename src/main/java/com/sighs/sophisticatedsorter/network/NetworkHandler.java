package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {
    public static final ResourceLocation SORT_PACKET_ID =
            new ResourceLocation(SophisticatedSorter.MODID, "server_sort");
    public static final ResourceLocation TRANSFER_PACKET_ID =
            new ResourceLocation(SophisticatedSorter.MODID, "server_transfer");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SORT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            ServerSortPacket packet = ServerSortPacket.decode(buf);
            server.execute(() -> packet.handle(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(TRANSFER_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            ServerTransferPacket packet = ServerTransferPacket.decode(buf);
            server.execute(() -> packet.handle(player));
        });
    }

    public static void sendToClient(ServerPlayer player, ResourceLocation id, Packet msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(PacketByteBufs.create());
        msg.encode(buf);
        ServerPlayNetworking.send(player, id, buf);
    }

    public static void sendToServer(ResourceLocation id, Packet msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(PacketByteBufs.create());
        msg.encode(buf);
        ClientPlayNetworking.send(id, buf);
    }
}
