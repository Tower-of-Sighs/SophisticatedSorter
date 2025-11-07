package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ServerTransferPacket(boolean transferToContainer, boolean filter) implements Packet {

    public static final Type<ServerTransferPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "server_transfer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerTransferPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerTransferPacket::transferToContainer,
            ByteBufCodecs.BOOL, ServerTransferPacket::filter,
            ServerTransferPacket::new
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends Packet> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static ServerTransferPacket decode(FriendlyByteBuf buf) {
        return new ServerTransferPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(ServerPlayer player, ServerTransferPacket msg) {
        if (player != null) {
            CoreUtils.transfer(player, msg.transferToContainer, msg.filter);
        }
    }
}