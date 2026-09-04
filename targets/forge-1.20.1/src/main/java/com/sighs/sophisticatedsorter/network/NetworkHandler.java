package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SophisticatedSorter.MODID, "sync_data"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    // 注册数据包
    public static void register() {
        int packetId = 0;
        CHANNEL.registerMessage(packetId++, ServerSortPacket.class, ServerSortPacket::encode, ServerSortPacket::decode, ServerSortPacket::handle);
        CHANNEL.registerMessage(packetId++, ServerTransferPacket.class, ServerTransferPacket::encode, ServerTransferPacket::decode, ServerTransferPacket::handle);
        CHANNEL.registerMessage(packetId++, ClientOpenContainerSettingsPayload.class, ClientOpenContainerSettingsPayload::encode, ClientOpenContainerSettingsPayload::decode, ClientOpenContainerSettingsPayload::handle);
        CHANNEL.registerMessage(packetId++, ClientCloseContainerSettingsPayload.class, ClientCloseContainerSettingsPayload::encode, ClientCloseContainerSettingsPayload::decode, ClientCloseContainerSettingsPayload::handle);
        CHANNEL.registerMessage(packetId++, ClientboundTrackedContainerKeyPayload.class, ClientboundTrackedContainerKeyPayload::encode, ClientboundTrackedContainerKeyPayload::decode, ClientboundTrackedContainerKeyPayload::handle);
        CHANNEL.registerMessage(packetId++, ClientboundContainerSettingsPayload.class, ClientboundContainerSettingsPayload::encode, ClientboundContainerSettingsPayload::decode, ClientboundContainerSettingsPayload::handle);
        CHANNEL.registerMessage(packetId++, ClientboundOpenContainerSettingsPayload.class, ClientboundOpenContainerSettingsPayload::encode, ClientboundOpenContainerSettingsPayload::decode, ClientboundOpenContainerSettingsPayload::handle);
    }

    // 发送数据包到客户端
    public static void sendToClient(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}