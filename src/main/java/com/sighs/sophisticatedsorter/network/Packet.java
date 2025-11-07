package com.sighs.sophisticatedsorter.network;

import net.minecraft.network.FriendlyByteBuf;

public interface Packet {
    void encode(FriendlyByteBuf buf);
}