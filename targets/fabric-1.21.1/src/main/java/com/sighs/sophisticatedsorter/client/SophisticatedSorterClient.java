package com.sighs.sophisticatedsorter.client;

import com.sighs.sophisticatedsorter.event.KeyInput;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.PlatformClient;
import net.fabricmc.api.ClientModInitializer;

public class SophisticatedSorterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientUtils.installPlatform(PlatformClient.INSTANCE);
        ModKeybindings.registerKeyMapping();
        KeyInput.register();
    }
}
