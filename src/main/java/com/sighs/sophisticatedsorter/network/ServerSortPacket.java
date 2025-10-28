package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

import java.util.function.Supplier;

public class ServerSortPacket {
    private final String sortBy;

    public ServerSortPacket(String sortBy) {
        this.sortBy = sortBy;
    }

    public static void encode(ServerSortPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.sortBy);
    }

    public static ServerSortPacket decode(FriendlyByteBuf buffer) {
        return new ServerSortPacket(buffer.readUtf());
    }

    public static void handle(ServerSortPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                CoreUtils.sortPlayer(player, SortBy.fromName(msg.sortBy));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
