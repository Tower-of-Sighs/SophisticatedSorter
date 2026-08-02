package com.sighs.sophisticatedsorter.visual;

import com.sighs.sophisticatedsorter.common.VisualSettingsBehavior;
import net.minecraft.nbt.CompoundTag;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.ISettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.main.MainSettingsCategory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VisualSettingsHandler extends SettingsHandler {
    private static final VisualSettingsBehavior<CompoundTag, InventoryHandler, RenderInfo,
            ISettingsCategory<?>, MainSettingsCategory<?>> BEHAVIOR = new VisualSettingsBehavior<>(
            () -> new MainSettingsCategory<>(new CompoundTag(), n -> {}, ""));

    protected VisualSettingsHandler() {
        super(new CompoundTag(), () -> {}, () -> null, () -> null);
    }

    @Override
    protected CompoundTag getSettingsNbtFromContentsNbt(CompoundTag compoundTag) {
        return BEHAVIOR.getSettingsNbtFromContentsNbt(compoundTag);
    }

    @Override
    protected void addItemDisplayCategory(Supplier<InventoryHandler> supplier,
            Supplier<RenderInfo> renderInfoSupplier, CompoundTag compoundTag) {
        BEHAVIOR.addItemDisplayCategory(supplier, renderInfoSupplier, compoundTag);
    }

    @Override
    public String getGlobalSettingsCategoryName() {
        return BEHAVIOR.getGlobalSettingsCategoryName();
    }

    @Override
    public ISettingsCategory<?> instantiateGlobalSettingsCategory(CompoundTag compoundTag,
            Consumer<CompoundTag> consumer) {
        return BEHAVIOR.instantiateGlobalSettingsCategory(compoundTag, consumer);
    }

    @Override
    protected void saveCategoryNbt(CompoundTag compoundTag, String name, CompoundTag categoryNbt) {
        BEHAVIOR.saveCategoryNbt(compoundTag, name, categoryNbt);
    }

    @Override
    public MainSettingsCategory<?> getGlobalSettingsCategory() {
        return BEHAVIOR.getGlobalSettingsCategory();
    }
}
