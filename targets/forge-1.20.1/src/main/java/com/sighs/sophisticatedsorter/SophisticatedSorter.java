package com.sighs.sophisticatedsorter;

import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.settings.ContainerMemorySlotGuard;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import com.sighs.sophisticatedsorter.settings.ModMenus;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import com.sighs.sophisticatedsorter.utils.PlatformSortBackend;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SophisticatedSorter.MODID)
public class SophisticatedSorter {

    public static final String MODID = "sophisticatedsorter";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SophisticatedSorter() {
        CoreUtils.installPlatform(PlatformSortBackend.INSTANCE);
        NetworkHandler.register();
        ModMenus.register(FMLJavaModLoadingContext.get().getModEventBus());
        ContainerSettingsTracker.register();
        ContainerMemorySlotGuard.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
    }
}
