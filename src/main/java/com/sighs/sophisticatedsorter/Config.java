package com.sighs.sophisticatedsorter;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = SophisticatedSorter.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<String> SORT_BY;
    public static ForgeConfigSpec.ConfigValue<Boolean> FILTER1;
    public static ForgeConfigSpec.ConfigValue<Boolean> FILTER2;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static ForgeConfigSpec.ConfigValue<Boolean> PINYIN;

    static {
        BUILDER.push("Sorter Setting");

        SORT_BY = BUILDER
                .comment("name, mod, count, tags")
                .define("SortBy", "name");
        FILTER1 = BUILDER
                .comment("Only valid for containers with more than 10 slots if true.")
                .define("Filter1", true);
        FILTER2 = BUILDER
                .comment("Only valid for containers without invalid slot such as recipe result slot if true.")
                .define("Filter2", true);
        BLACKLIST = BUILDER
                .comment("Special of screens.")
                .defineList("specialList",
                        List.of(),
                        entry -> entry instanceof String
                );
        PINYIN = BUILDER
                .comment("是否启用默认拼音排序。")
                .define("pinyin", true);

        SPEC = BUILDER.build();
    }
}
