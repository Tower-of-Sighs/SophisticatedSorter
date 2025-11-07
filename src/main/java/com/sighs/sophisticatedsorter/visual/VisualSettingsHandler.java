package com.sighs.sophisticatedsorter.visual;

import net.minecraft.nbt.CompoundTag;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.ISettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsCategory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VisualSettingsHandler extends SettingsHandler {
    protected VisualSettingsHandler() {
        super(new CompoundTag(), () -> {}, () -> null, () -> null);
    }

    @Override
    protected CompoundTag getSettingsNbtFromContentsNbt(CompoundTag compoundTag) {
        return null;
    }

    @Override
    protected void addItemDisplayCategory(Supplier<InventoryHandler> supplier, Supplier<RenderInfo> supplier1, CompoundTag compoundTag) {

    }

    @Override
    public String getGlobalSettingsCategoryName() {
        return "";
    }

    @Override
    public ISettingsCategory<?> instantiateGlobalSettingsCategory(CompoundTag compoundTag, Consumer<CompoundTag> consumer) {
        return null;
    }

    @Override
    protected void saveCategoryNbt(CompoundTag compoundTag, String s, CompoundTag compoundTag1) {

    }

    @Override
    public MainSettingsCategory<?> getGlobalSettingsCategory() {
        return new MainSettingsCategory<>(new CompoundTag(), n -> {}, "");
    }
}
