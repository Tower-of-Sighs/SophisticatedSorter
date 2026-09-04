package com.sighs.sophisticatedsorter;

import com.mojang.logging.LogUtils;
import com.sighs.sophisticatedsorter.settings.ContainerMemorySlotGuard;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import com.sighs.sophisticatedsorter.settings.ModMenus;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import com.sighs.sophisticatedsorter.utils.PlatformSortBackend;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SophisticatedSorter.MODID)
public class SophisticatedSorter {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "sophisticatedsorter";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public SophisticatedSorter(IEventBus modEventBus, ModContainer modContainer) {
        CoreUtils.installPlatform(PlatformSortBackend.INSTANCE);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        ModMenus.register(modEventBus);
        ContainerSettingsTracker.register(NeoForge.EVENT_BUS);
        ContainerMemorySlotGuard.register();
        if (modContainer.getModInfo() != null && FMLEnvironment.getDist() == Dist.CLIENT) {
            com.sighs.sophisticatedsorter.client.ClientModSetup.init(modEventBus);
        }
    }
}