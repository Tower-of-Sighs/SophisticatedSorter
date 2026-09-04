package com.sighs.sophisticatedsorter;

import com.sighs.sophisticatedsorter.common.ButtonPositionCodec;
import com.sighs.sophisticatedsorter.common.ScreenId;
import com.sighs.sophisticatedsorter.common.ButtonPositions;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;

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
    public static ModConfigSpec.ConfigValue<List<? extends String>> BUTTON_POSITIONS;

    /**
     * Server-side config that hosts the Sophisticated Core {@link StackUpgradeConfig}. Core's
     * {@code InventoryHandler} asks its stack-upgrade config for item stack limits whenever the
     * handler has real slots, and {@code StackUpgradeConfig.canStackItem} only returns safely when
     * the value it reads belongs to a registered (and therefore loaded) spec - mirroring how
     * Sophisticated Backpacks hosts its {@code StackUpgradeConfig} in its server config.
     */
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

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
                .comment("Screens whose sorter buttons are hidden (toggle with the disable-buttons key). Entries are \"screenId\" = screen class + \"@\" + title translation key.")
                .defineList("specialList",
                        List.of(),
                        entry -> entry instanceof String
                );
        PINYIN = BUILDER
                .comment("是否启用默认拼音排序。")
                .define("pinyin", true);

        BUTTON_POSITIONS = BUILDER
                .comment("Per-screen button offsets: screenId (screen class + \"@\" + title key)|sort X|sort Y|transfer X|transfer Y.")
                .defineList("buttonPositions",
                        List.of(),
                        entry -> entry instanceof String
                );

        SPEC = BUILDER.build();

        var serverSpec = new ModConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverSpec.getRight();
        SERVER = serverSpec.getLeft();
    }

    public static final class Server {
        public final StackUpgradeConfig stackUpgrade;

        public Server(ModConfigSpec.Builder builder) {
            this.stackUpgrade = new StackUpgradeConfig(builder);
        }
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
            if (!ScreenId.matches(fields[0], screenType)) {
                records.add(record);
            }
        }
        records.add(ButtonPositionCodec.format(screenType, positions));
        BUTTON_POSITIONS.set(records);
        BUTTON_POSITIONS.save();
    }
}
