package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -&gt; client: the authoritative settings contents of one container. Sent when a container is
 * opened (so highlights/ghosts have data) and whenever its settings change (so the open settings
 * screen and the normal container screen stay in sync). A null contents clears the client's mirror
 * for that key.
 */
public class ClientboundContainerSettingsPayload {
    private final ContainerSettingsKey key;
    @Nullable
    private final CompoundTag contents;

    public ClientboundContainerSettingsPayload(ContainerSettingsKey key, @Nullable CompoundTag contents) {
        this.key = key;
        this.contents = contents;
    }

    public static void encode(ClientboundContainerSettingsPayload msg, FriendlyByteBuf buffer) {
        msg.key.write(buffer);
        buffer.writeNbt(msg.contents);
    }

    public static ClientboundContainerSettingsPayload decode(FriendlyByteBuf buffer) {
        ContainerSettingsKey key = ContainerSettingsKey.fromBuffer(buffer);
        CompoundTag contents = buffer.readNbt();
        return new ClientboundContainerSettingsPayload(key, contents);
    }

    public static void handle(ClientboundContainerSettingsPayload msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.contents == null) {
                ClientContainerSettingsCache.remove(msg.key);
            } else {
                ClientContainerSettingsCache.putContents(msg.key, msg.contents);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}