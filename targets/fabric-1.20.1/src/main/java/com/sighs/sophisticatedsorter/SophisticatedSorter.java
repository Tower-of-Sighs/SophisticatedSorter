package com.sighs.sophisticatedsorter;

import com.mojang.logging.LogUtils;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import com.sighs.sophisticatedsorter.settings.ModMenus;
import com.sighs.sophisticatedsorter.settings.ServerContainerSettingsStore;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import com.sighs.sophisticatedsorter.utils.PlatformSortBackend;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class SophisticatedSorter implements ModInitializer {
    public static final String MODID = "sophisticatedsorter";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        CoreUtils.installPlatform(PlatformSortBackend.INSTANCE);
        ModConfig.register();
        Config.register();
        NetworkHandler.register();
        ModMenus.register();
        ContainerSettingsTracker.register();
        ServerContainerSettingsStore.register();
    }
}