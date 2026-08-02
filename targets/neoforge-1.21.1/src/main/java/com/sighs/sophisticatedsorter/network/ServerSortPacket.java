package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.platform.NeoForgeSorterCommands;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerSortPacket(String sortBy, String target, boolean zh) implements CustomPacketPayload {
    public ServerSortPacket(SortRequest request) {
        this(request.criterion().wireName(), request.target().wireName(), request.pinyinOrder());
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "server_sort");
    public static final CustomPacketPayload.Type<ServerSortPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, ServerSortPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ServerSortPacket::sortBy,
            ByteBufCodecs.STRING_UTF8,
            ServerSortPacket::target,
            ByteBufCodecs.BOOL,
            ServerSortPacket::zh,
            ServerSortPacket::new
    );

    public static void execute(ServerSortPacket msg, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        NeoForgeSorterCommands.INSTANCE.sort(player, SortRequest.fromWire(msg.sortBy, msg.target, msg.zh));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
