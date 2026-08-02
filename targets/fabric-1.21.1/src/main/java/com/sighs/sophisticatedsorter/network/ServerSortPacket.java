package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.platform.FabricSorterCommands;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Fabric 1.21.1 C2S sort payload. */
public record ServerSortPacket(String sortBy, String target, boolean pinyinOrder) implements CustomPacketPayload {
    public ServerSortPacket(SortRequest request) {
        this(request.criterion().wireName(), request.target().wireName(), request.pinyinOrder());
    }

    public static final Type<ServerSortPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "server_sort"));
    public static final StreamCodec<ByteBuf, ServerSortPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerSortPacket::sortBy,
            ByteBufCodecs.STRING_UTF8, ServerSortPacket::target,
            ByteBufCodecs.BOOL, ServerSortPacket::pinyinOrder,
            ServerSortPacket::new);

    public void handle(ServerPlayer player) {
        FabricSorterCommands.INSTANCE.sort(player, SortRequest.fromWire(sortBy, target, pinyinOrder));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
