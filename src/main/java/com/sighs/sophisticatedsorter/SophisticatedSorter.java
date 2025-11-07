package com.sighs.sophisticatedsorter;

import com.sighs.sophisticatedsorter.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;

public class SophisticatedSorter implements ModInitializer {
    public static final String MODID = "sophisticatedsorter";

    @Override
    public void onInitialize() {
        ModConfig.register();
        NetworkHandler.register();
    }
}
