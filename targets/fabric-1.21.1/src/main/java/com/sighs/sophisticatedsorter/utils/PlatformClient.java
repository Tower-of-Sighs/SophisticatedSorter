package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.ModConfig;
import com.sighs.sophisticatedsorter.common.ButtonPositionCodec;
import com.sighs.sophisticatedsorter.common.ButtonPositions;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.network.ServerSortPacket;
import com.sighs.sophisticatedsorter.network.ServerTransferPacket;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import me.shedaniel.autoconfig.AutoConfig;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

import java.util.ArrayList;
import java.util.List;

/** Fabric 1.21.1 AutoConfig and typed payload transport. */
public final class PlatformClient implements ClientPlatform {
    public static final ClientPlatform INSTANCE = new PlatformClient();

    private PlatformClient() {
    }

    @Override
    public boolean isScreenDisabled(String screenId) {
        return ModConfig.INSTANCE.BLACK_LIST.contains(screenId);
    }

    @Override
    public boolean isFilter1Enabled() {
        return ModConfig.INSTANCE.FILTER1;
    }

    @Override
    public boolean isFilter2Enabled() {
        return ModConfig.INSTANCE.FILTER2;
    }

    @Override
    public boolean isPinyinEnabled() {
        return ModConfig.INSTANCE.PINYIN;
    }

    @Override
    public SortBy getSortBy() {
        return ModConfig.INSTANCE.SORT_BY;
    }

    @Override
    public void toggleSortBy() {
        ModConfig.INSTANCE.SORT_BY = getSortBy().next();
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    @Override
    public void sendSort(SortRequest request) {
        NetworkHandler.sendToServer(new ServerSortPacket(request));
    }

    @Override
    public void sendTransfer(TransferRequest request) {
        NetworkHandler.sendToServer(new ServerTransferPacket(request));
    }

    @Override
    public String getDisableKeyDisplayName() {
        return KeyDisplayNames.displayName(ModKeybindings.DISABLE_KEY);
    }

    @Override
    public ButtonPositions getButtonPositions(String screenType) {
        ButtonPositions result = ButtonPositions.ZERO;
        for (String record : ModConfig.INSTANCE.BUTTON_POSITIONS) {
            ButtonPositions parsed = ButtonPositionCodec.parse(record, screenType);
            if (parsed != null) {
                result = parsed;
            }
        }
        return result;
    }

    @Override
    public void saveButtonPositions(String screenType, ButtonPositions positions) {
        List<String> records = new ArrayList<>();
        for (String record : ModConfig.INSTANCE.BUTTON_POSITIONS) {
            if (ButtonPositionCodec.parse(record, null) == null) {
                continue;
            }
            String[] fields = record.split("\\|", -1);
            if (!fields[0].equals(screenType)) {
                records.add(record);
            }
        }
        records.add(ButtonPositionCodec.format(screenType, positions));
        ModConfig.INSTANCE.BUTTON_POSITIONS = records;
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }
}
