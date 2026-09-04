package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.client.settings.ClientTrackedContainer;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKeyCodec;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -&gt; client: tells the client which container the player just opened (resolved server-side,
 * where menu slots reference the real block entity). The client stores it so vanilla container
 * screens can render the per-slot settings highlights; a null key clears the record (menu closed or
 * not a supported container).
 */
public class ClientboundTrackedContainerKeyPayload {
    @Nullable
    private final ContainerSettingsKey key;

    public ClientboundTrackedContainerKeyPayload(@Nullable ContainerSettingsKey key) {
        this.key = key;
    }

    public static void encode(ClientboundTrackedContainerKeyPayload msg, FriendlyByteBuf buffer) {
        ContainerSettingsKeyCodec.writeNullable(msg.key, buffer);
    }

    public static ClientboundTrackedContainerKeyPayload decode(FriendlyByteBuf buffer) {
        return new ClientboundTrackedContainerKeyPayload(ContainerSettingsKeyCodec.readNullable(buffer));
    }

    public static void handle(ClientboundTrackedContainerKeyPayload msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientTrackedContainer.setCurrentKey(msg.key));
        ctx.get().setPacketHandled(true);
    }
}