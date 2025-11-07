package com.sighs.sophisticatedsorter.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface Packet extends CustomPacketPayload {
    StreamCodec<RegistryFriendlyByteBuf, ? extends Packet> getStreamCodec();
}