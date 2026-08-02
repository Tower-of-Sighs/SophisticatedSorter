package com.sighs.sophisticatedsorter;

import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import com.sighs.sophisticatedsorter.utils.PlatformSortBackend;
import net.fabricmc.api.ModInitializer;

public class SophisticatedSorter implements ModInitializer {
    public static final String MODID = "sophisticatedsorter";
    @Override
    public void onInitialize() {
        CoreUtils.installPlatform(PlatformSortBackend.INSTANCE);
        ModConfig.register();
        NetworkHandler.register();
    }
}
