package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.ModConfig;
import com.sighs.sophisticatedsorter.common.ButtonPositionCodec;
import com.sighs.sophisticatedsorter.common.ScreenId;
import com.sighs.sophisticatedsorter.common.ButtonPositions;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.network.ClientOpenContainerSettingsPayload;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.network.ServerSortPacket;
import com.sighs.sophisticatedsorter.network.ServerTransferPacket;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import me.shedaniel.autoconfig.AutoConfig;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

import java.util.ArrayList;
import java.util.List;

/** Fabric 1.20.1 AutoConfig and identifier packet transport. */
public final class PlatformClient implements ClientPlatform {
    public static final ClientPlatform INSTANCE = new PlatformClient();

    private PlatformClient() {
    }

    @Override
    public boolean isScreenDisabled(String screenId) {
        return ModConfig.INSTANCE.BLACK_LIST.stream().anyMatch(entry -> ScreenId.matches(entry, screenId));
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
        NetworkHandler.sendToServer(NetworkHandler.SORT_PACKET_ID, new ServerSortPacket(request));
    }

    @Override
    public void sendTransfer(TransferRequest request) {
        NetworkHandler.sendToServer(NetworkHandler.TRANSFER_PACKET_ID, new ServerTransferPacket(request));
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
            if (!ScreenId.matches(fields[0], screenType)) {
                records.add(record);
            }
        }
        records.add(ButtonPositionCodec.format(screenType, positions));
        ModConfig.INSTANCE.BUTTON_POSITIONS = records;
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    @Override
    public boolean hasContainerSettings() {
        return true;
    }

    @Override
    public void openSettingsRequested(boolean playerInventoryScreen) {
        // The settings screen is opened through a server menu swap; the server resolves the target
        // from its tracker for container screens, while the player-inventory main screen carries an
        // explicit key (the server cannot derive a block position from the inventory menu).
        if (playerInventoryScreen) {
            NetworkHandler.sendToServer(NetworkHandler.OPEN_CONTAINER_SETTINGS_ID,
                    new ClientOpenContainerSettingsPayload(ContainerSettingsKey.playerInventory()));
        } else {
            NetworkHandler.sendToServer(NetworkHandler.OPEN_CONTAINER_SETTINGS_ID, new ClientOpenContainerSettingsPayload());
        }
    }
}
