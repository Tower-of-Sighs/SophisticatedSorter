package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

import java.util.function.Supplier;

public class ServerSortPacket {
    private final String sortBy;
    private final String target;

    public ServerSortPacket(String sortBy, String target) {
        this.sortBy = sortBy;
        this.target = target;
    }

    public static void encode(ServerSortPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.sortBy);
        buffer.writeUtf(msg.target);
    }

    public static ServerSortPacket decode(FriendlyByteBuf buffer) {
        return new ServerSortPacket(buffer.readUtf(), buffer.readUtf());
    }

    public static void handle(ServerSortPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (msg.target.equals("inventory")) CoreUtils.sortInventory(player, SortBy.fromName(msg.sortBy));
                if (msg.target.equals("container")) CoreUtils.sortContainer(player, SortBy.fromName(msg.sortBy));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
