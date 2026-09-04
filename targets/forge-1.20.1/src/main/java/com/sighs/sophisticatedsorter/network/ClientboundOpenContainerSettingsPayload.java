package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server -&gt; client: the data the client-side settings menu constructor needs to rebuild the
 * settings wrapper - the {@link ContainerSettingsKey}, the real slot count of the container and its
 * current settings contents. Forge 1.20.1's open-screen packet carries no extra data, so the server
 * pushes this payload over the mod channel right before opening the settings menu
 * ({@link com.sighs.sophisticatedsorter.settings.ContainerOpenFlow}); the client stages it through
 * {@link ClientContainerSettingsCache#stageOpenData} and
 * {@link com.sighs.sophisticatedsorter.settings.ContainerSettingsContainerMenu#fromBuffer} consumes
 * it (both travel on the same connection, in order).
 */
public class ClientboundOpenContainerSettingsPayload {
    private final ContainerSettingsKey key;
    private final int slots;
    @Nullable
    private final CompoundTag contents;

    public ClientboundOpenContainerSettingsPayload(ContainerSettingsKey key, int slots, @Nullable CompoundTag contents) {
        this.key = key;
        this.slots = slots;
        this.contents = contents;
    }

    public static void encode(ClientboundOpenContainerSettingsPayload msg, FriendlyByteBuf buffer) {
        msg.key.write(buffer);
        buffer.writeVarInt(msg.slots);
        buffer.writeNbt(msg.contents);
    }

    public static ClientboundOpenContainerSettingsPayload decode(FriendlyByteBuf buffer) {
        ContainerSettingsKey key = ContainerSettingsKey.fromBuffer(buffer);
        int slots = buffer.readVarInt();
        CompoundTag contents = buffer.readNbt();
        return new ClientboundOpenContainerSettingsPayload(key, slots, contents);
    }

    public static void handle(ClientboundOpenContainerSettingsPayload msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientContainerSettingsCache.stageOpenData(msg.key, msg.slots, msg.contents));
        ctx.get().setPacketHandled(true);
    }
}