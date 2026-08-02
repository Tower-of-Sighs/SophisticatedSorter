package com.sighs.sophisticatedsorter;

import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import com.sighs.sophisticatedsorter.utils.PlatformSortBackend;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(SophisticatedSorter.MODID)
public class SophisticatedSorter {

    public static final String MODID = "sophisticatedsorter";

    public SophisticatedSorter() {
        CoreUtils.installPlatform(PlatformSortBackend.INSTANCE);
        NetworkHandler.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
