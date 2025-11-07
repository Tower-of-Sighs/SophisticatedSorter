package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

public record ServerSortPacket(String sortBy, String target, boolean zh) implements Packet {
    public static final Type<ServerSortPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "server_sort"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerSortPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerSortPacket::sortBy,
            ByteBufCodecs.STRING_UTF8, ServerSortPacket::target,
            ByteBufCodecs.BOOL, ServerSortPacket::zh,
            ServerSortPacket::new
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Packet> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerPlayer player, ServerSortPacket msg) {
        if (player == null) return;
        if (msg.target.equals("inventory")) {
            CoreUtils.sortInventory(player, SortBy.fromName(msg.sortBy), msg.zh);
        } else if (msg.target.equals("container")) {
            CoreUtils.sortContainer(player, SortBy.fromName(msg.sortBy), msg.zh);
        }
    }
}