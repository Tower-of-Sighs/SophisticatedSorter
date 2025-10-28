package com.sighs.sophisticatedsorter;

import com.sighs.sophisticatedsorter.network.NetworkHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(SophisticatedSorter.MODID)
public class SophisticatedSorter {

    public static final String MODID = "sophisticatedsorter";

    public SophisticatedSorter() {
        NetworkHandler.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
