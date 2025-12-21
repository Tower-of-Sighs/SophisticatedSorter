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

public record ServerTransferPacket(boolean transferToContainer, boolean filter) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "server_transfer");
    public static final Type<ServerTransferPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, ServerTransferPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ServerTransferPacket::transferToContainer,
            ByteBufCodecs.BOOL,
            ServerTransferPacket::filter,
            ServerTransferPacket::new
    );

    public static void execute(ServerTransferPacket msg, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        CoreUtils.transfer(player, msg.transferToContainer, msg.filter);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
