package com.sighs.sophisticatedsorter.network;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SophisticatedSorter.MODID)
public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(SophisticatedSorter.MODID);
        registrar.playToServer(ServerSortPacket.TYPE, ServerSortPacket.STREAM_CODEC, ServerSortPacket::execute);
        registrar.playToServer(ServerTransferPacket.TYPE, ServerTransferPacket.STREAM_CODEC, ServerTransferPacket::execute);
    }
}