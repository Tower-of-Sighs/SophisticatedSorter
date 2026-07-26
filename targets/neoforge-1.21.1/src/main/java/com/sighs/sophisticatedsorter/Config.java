package com.sighs.sophisticatedsorter;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.ConfigValue<String> SORT_BY;
    public static ModConfigSpec.ConfigValue<Boolean> FILTER1;
    public static ModConfigSpec.ConfigValue<Boolean> FILTER2;
    public static ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static ModConfigSpec.ConfigValue<Boolean> PINYIN;

    static {
        BUILDER.push("Setting");

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
