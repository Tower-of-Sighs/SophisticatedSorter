package com.sighs.sophisticatedsorter;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SophisticatedSorter.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<String> SORT_BY;
    public static ForgeConfigSpec.ConfigValue<Boolean> Filter;

    static {
        BUILDER.push("Sorter Setting");

        SORT_BY = BUILDER
                .comment("name, mod, count, tags")
                .define("SortBy", "name");
        Filter = BUILDER
                .comment("Only valid for containers with more than 10 slots if true.")
                .define("Filter", true);

        SPEC = BUILDER.build();
    }
}
