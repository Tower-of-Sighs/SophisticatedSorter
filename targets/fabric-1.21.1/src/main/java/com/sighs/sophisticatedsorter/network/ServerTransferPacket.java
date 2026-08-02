package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.platform.FabricSorterCommands;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Fabric 1.21.1 C2S transfer payload. */
public record ServerTransferPacket(boolean toContainer, boolean filterByDestination) implements CustomPacketPayload {
    public ServerTransferPacket(TransferRequest request) {
        this(request.toContainer(), request.filterByDestination());
    }

    public static final Type<ServerTransferPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "server_transfer"));
    public static final StreamCodec<ByteBuf, ServerTransferPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerTransferPacket::toContainer,
            ByteBufCodecs.BOOL, ServerTransferPacket::filterByDestination,
            ServerTransferPacket::new);

    public void handle(ServerPlayer player) {
        FabricSorterCommands.INSTANCE.transfer(player, new TransferRequest(toContainer, filterByDestination));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
