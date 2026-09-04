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
    public static final ResourceLocation OPEN_CONTAINER_SETTINGS_ID =
            new ResourceLocation(SophisticatedSorter.MODID, "open_container_settings");
    public static final ResourceLocation CLOSE_CONTAINER_SETTINGS_ID =
            new ResourceLocation(SophisticatedSorter.MODID, "close_container_settings");
    public static final ResourceLocation TRACKED_CONTAINER_KEY_ID =
            new ResourceLocation(SophisticatedSorter.MODID, "tracked_container_key");
    public static final ResourceLocation CONTAINER_SETTINGS_ID =
            new ResourceLocation(SophisticatedSorter.MODID, "container_settings_contents");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SORT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            ServerSortPacket packet = ServerSortPacket.decode(buf);
            server.execute(() -> packet.handle(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(TRANSFER_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            ServerTransferPacket packet = ServerTransferPacket.decode(buf);
            server.execute(() -> packet.handle(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(OPEN_CONTAINER_SETTINGS_ID, (server, player, handler, buf, responseSender) -> {
            ClientOpenContainerSettingsPayload packet = ClientOpenContainerSettingsPayload.decode(buf);
            server.execute(() -> packet.handle(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(CLOSE_CONTAINER_SETTINGS_ID, (server, player, handler, buf, responseSender) -> {
            ClientCloseContainerSettingsPayload packet = ClientCloseContainerSettingsPayload.decode(buf);
            server.execute(() -> packet.handle(player));
        });
    }

    /** Client-side receivers for the server-to-client settings payloads (registered on the client). */
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(TRACKED_CONTAINER_KEY_ID, (client, handler, buf, responseSender) -> {
            ClientboundTrackedContainerKeyPayload packet = ClientboundTrackedContainerKeyPayload.decode(buf);
            client.execute(packet::handle);
        });

        ClientPlayNetworking.registerGlobalReceiver(CONTAINER_SETTINGS_ID, (client, handler, buf, responseSender) -> {
            ClientboundContainerSettingsPayload packet = ClientboundContainerSettingsPayload.decode(buf);
            client.execute(packet::handle);
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