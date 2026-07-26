package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.platform.ForgeSorterCommands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerTransferPacket {
    private final boolean transferToContainer;
    private final boolean filter;

    public ServerTransferPacket(boolean transferToContainer, boolean filter) {
        this.transferToContainer = transferToContainer;
        this.filter = filter;
    }

    public static void encode(ServerTransferPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.transferToContainer);
        buffer.writeBoolean(msg.filter);
    }

    public static ServerTransferPacket decode(FriendlyByteBuf buffer) {
        return new ServerTransferPacket(buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(ServerTransferPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) ForgeSorterCommands.INSTANCE.transfer(player, new TransferRequest(msg.transferToContainer, msg.filter));
        });
        ctx.get().setPacketHandled(true);
    }
}
