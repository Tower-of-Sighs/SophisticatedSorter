package com.sighs.sophisticatedsorter;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

import java.util.List;

@Config(name = SophisticatedSorter.MODID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Excluded
    public static ModConfig INSTANCE;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
    public SortBy SORT_BY = SortBy.NAME;

    @ConfigEntry.Gui.Tooltip
    public boolean FILTER1 = true;

    @ConfigEntry.Gui.Tooltip
    public boolean FILTER2 = true;

    @ConfigEntry.Gui.Tooltip
    public List<String> BLACK_LIST = List.of();

    @ConfigEntry.Gui.Tooltip
    public boolean PINYIN = true;

    public static void register() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
