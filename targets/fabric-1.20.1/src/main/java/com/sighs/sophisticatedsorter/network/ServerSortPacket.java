package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.platform.FabricSorterCommands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ServerSortPacket implements Packet {
    private final String sortBy;
    private final String target;
    private final boolean zh;

    public ServerSortPacket(String sortBy, String target, boolean zh) {
        this.sortBy = sortBy;
        this.target = target;
        this.zh = zh;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(sortBy);
        buf.writeUtf(target);
        buf.writeBoolean(zh);
    }

    public static ServerSortPacket decode(FriendlyByteBuf buf) {
        return new ServerSortPacket(buf.readUtf(), buf.readUtf(), buf.readBoolean());
    }

    public void handle(ServerPlayer player) {
        if (player == null) return;
        FabricSorterCommands.INSTANCE.sort(player, SortRequest.fromWire(sortBy, target, zh));
    }
}
