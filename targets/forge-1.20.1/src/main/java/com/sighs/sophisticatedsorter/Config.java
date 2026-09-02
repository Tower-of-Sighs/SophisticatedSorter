package com.sighs.sophisticatedsorter;

import com.sighs.sophisticatedsorter.common.ButtonPositionCodec;
import com.sighs.sophisticatedsorter.common.ButtonPositions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
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
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BUTTON_POSITIONS;

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

        BUTTON_POSITIONS = BUILDER
                .comment("Per-screen button offsets: screen class|sort X|sort Y|transfer X|transfer Y.")
                .defineList("buttonPositions",
                        List.of(),
                        entry -> entry instanceof String
                );

        SPEC = BUILDER.build();
    }

    /**
     * Returns the offsets saved for one concrete screen class. Invalid records are ignored and
     * duplicate records use the last valid value, which makes hand-edited config files harmless.
     */
    public static ButtonPositions getButtonPositions(String screenType) {
        ButtonPositions result = ButtonPositions.ZERO;
        for (String record : BUTTON_POSITIONS.get()) {
            ButtonPositions parsed = ButtonPositionCodec.parse(record, screenType);
            if (parsed != null) {
                result = parsed;
            }
        }
        return result;
    }

    /**
     * Replaces the offsets for one screen class while preserving other screen classes' records.
     * Invalid records and duplicate entries for the updated screen are removed on save.
     */
    public static void saveButtonPositions(String screenType, ButtonPositions positions) {
        List<String> records = new ArrayList<>();
        for (String record : BUTTON_POSITIONS.get()) {
            if (ButtonPositionCodec.parse(record, null) == null) {
                continue;
            }
            String[] fields = record.split("\\|", -1);
            if (!fields[0].equals(screenType)) {
                records.add(record);
            }
        }
        records.add(ButtonPositionCodec.format(screenType, positions));
        BUTTON_POSITIONS.set(records);
        BUTTON_POSITIONS.save();
    }
}
