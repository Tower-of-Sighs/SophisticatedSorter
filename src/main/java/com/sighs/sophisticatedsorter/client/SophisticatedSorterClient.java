package com.sighs.sophisticatedsorter.client;

import com.sighs.sophisticatedsorter.event.KeyInput;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import net.fabricmc.api.ClientModInitializer;

public class SophisticatedSorterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModKeybindings.registerKeyMapping();
        KeyInput.register();
    }
}
