package com.sighs.sophisticatedsorter;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;
import com.sighs.sophisticatedsorter.common.ScreenId;

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
     * Whether sorting should refill memorized slots with the items they are set to remember - the
     * behavior of core's pre-26.1 sorter (and of the 1.20.1/1.21.1 targets). 26.1's own sorter
     * dropped memory-slot handling, so when this is disabled memorized slots are sorted like
     * ordinary slots (only the placement guard keeps non-matching items out).
     */
    public static ModConfigSpec.BooleanValue MEMORY_SLOT_SORTING;

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
                .defineList("buttonPositions", List.of(), entry -> entry instanceof String);

        MEMORY_SLOT_SORTING = BUILDER
                .comment("When true (default) sorting puts each memorized slot's remembered item into that slot, "
                        + "matching the pre-26.1 behavior; set to false to sort memory slots like ordinary slots.")
                .define("memorySlotSorting", true);

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
            ButtonPositions parsed = parseButtonPosition(record, screenType);
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
    public static void saveButtonPositions(String screenType, int sortX, int sortY,
                                            int transferX, int transferY) {
        List<String> records = new ArrayList<>();
        for (String record : BUTTON_POSITIONS.get()) {
            if (parseButtonPosition(record, null) == null) {
                continue;
            }
            String[] fields = record.split("\\|", -1);
            if (!ScreenId.matches(fields[0], screenType)) {
                records.add(record);
            }
        }
        records.add(formatButtonPosition(screenType, sortX, sortY, transferX, transferY));
        BUTTON_POSITIONS.set(records);
        BUTTON_POSITIONS.save();
    }

    private static ButtonPositions parseButtonPosition(String record, String screenType) {
        if (record == null) {
            return null;
        }
        String[] fields = record.split("\\|", -1);
        if (fields.length != 5 || fields[0].isEmpty() || (screenType != null && !ScreenId.matches(fields[0], screenType))) {
            return null;
        }
        try {
            return new ButtonPositions(
                    Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[2]),
                    Integer.parseInt(fields[3]),
                    Integer.parseInt(fields[4]));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String formatButtonPosition(String screenType, int sortX, int sortY,
                                               int transferX, int transferY) {
        return screenType + "|" + sortX + "|" + sortY + "|" + transferX + "|" + transferY;
    }

    public record ButtonPositions(int sortX, int sortY, int transferX, int transferY) {
        private static final ButtonPositions ZERO = new ButtonPositions(0, 0, 0, 0);
    }
}