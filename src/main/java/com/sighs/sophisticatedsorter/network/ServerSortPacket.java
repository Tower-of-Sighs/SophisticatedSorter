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
    private final boolean zh;

    public ServerSortPacket(String sortBy, String target, boolean zh) {
        this.sortBy = sortBy;
        this.target = target;
        this.zh = zh;
    }

    public static void encode(ServerSortPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.sortBy);
        buffer.writeUtf(msg.target);
        buffer.writeBoolean(msg.zh);
    }

    public static ServerSortPacket decode(FriendlyByteBuf buffer) {
        return new ServerSortPacket(buffer.readUtf(), buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(ServerSortPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (msg.target.equals("inventory")) CoreUtils.sortInventory(player, SortBy.fromName(msg.sortBy), msg.zh);
                if (msg.target.equals("container")) CoreUtils.sortContainer(player, SortBy.fromName(msg.sortBy), msg.zh);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
