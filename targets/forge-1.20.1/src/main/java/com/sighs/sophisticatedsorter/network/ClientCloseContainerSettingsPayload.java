package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.settings.ContainerOpenFlow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -&gt; server: "close the settings screen and return to the container I was viewing". Sent
 * by the settings screen on ESC and by the return tab. The server reopens the container recorded
 * in {@link com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker} for the player; when
 * the target block entity is gone it falls back to closing the container.
 */
public class ClientCloseContainerSettingsPayload {
    public ClientCloseContainerSettingsPayload() {
    }

    public static void encode(ClientCloseContainerSettingsPayload msg, FriendlyByteBuf buffer) {
    }

    public static ClientCloseContainerSettingsPayload decode(FriendlyByteBuf buffer) {
        return new ClientCloseContainerSettingsPayload();
    }

    public static void handle(ClientCloseContainerSettingsPayload msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ContainerOpenFlow.closeSettings(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}