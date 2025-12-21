package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

public record ServerSortPacket(String sortBy, String target, boolean zh) implements CustomPacketPayload {
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
        if (msg.target.equals("inventory")) CoreUtils.sortInventory(player, SortBy.fromName(msg.sortBy), msg.zh);
        if (msg.target.equals("container")) CoreUtils.sortContainer(player, SortBy.fromName(msg.sortBy), msg.zh);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
