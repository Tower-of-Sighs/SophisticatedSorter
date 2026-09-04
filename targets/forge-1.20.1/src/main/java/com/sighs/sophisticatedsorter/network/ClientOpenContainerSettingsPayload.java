package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.settings.ContainerOpenFlow;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKeyCodec;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -&gt; server: "open the container-settings screen for the container the player currently
 * has open". The payload is anonymous on purpose - the gear entry on a vanilla container screen
 * cannot know its own menu's dimension/position, so the server resolves the target from
 * {@link ContainerSettingsTracker}. An optional explicit key is accepted for callers that know
 * their target (and for the player-inventory case).
 */
public class ClientOpenContainerSettingsPayload {
    @Nullable
    private final ContainerSettingsKey key;

    public ClientOpenContainerSettingsPayload() {
        this((ContainerSettingsKey) null);
    }

    public ClientOpenContainerSettingsPayload(@Nullable ContainerSettingsKey key) {
        this.key = key;
    }

    public static void encode(ClientOpenContainerSettingsPayload msg, FriendlyByteBuf buffer) {
        ContainerSettingsKeyCodec.writeNullable(msg.key, buffer);
    }

    public static ClientOpenContainerSettingsPayload decode(FriendlyByteBuf buffer) {
        return new ClientOpenContainerSettingsPayload(ContainerSettingsKeyCodec.readNullable(buffer));
    }

    public static void handle(ClientOpenContainerSettingsPayload msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ContainerSettingsKey key = msg.key != null ? msg.key : ContainerSettingsTracker.get().getOpenKey(player);
                if (key != null) {
                    ContainerOpenFlow.openSettings(player, key);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}