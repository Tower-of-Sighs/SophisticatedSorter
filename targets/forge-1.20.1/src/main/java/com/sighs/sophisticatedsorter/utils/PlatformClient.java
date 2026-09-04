package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.Config;
import com.sighs.sophisticatedsorter.common.ButtonPositions;
import com.sighs.sophisticatedsorter.common.ScreenId;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.network.ServerSortPacket;
import com.sighs.sophisticatedsorter.network.ServerTransferPacket;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

/** Forge configuration holders and SimpleChannel transport. */
public final class PlatformClient implements ClientPlatform {
    public static final ClientPlatform INSTANCE = new PlatformClient();

    private PlatformClient() {
    }

    @Override
    public boolean isScreenDisabled(String screenId) {
        return Config.BLACKLIST.get().stream().anyMatch(entry -> ScreenId.matches(entry, screenId));
    }

    @Override
    public boolean isFilter1Enabled() {
        return Config.FILTER1.get();
    }

    @Override
    public boolean isFilter2Enabled() {
        return Config.FILTER2.get();
    }

    @Override
    public boolean isPinyinEnabled() {
        return Config.PINYIN.get();
    }

    @Override
    public SortBy getSortBy() {
        return SortBy.fromName(Config.SORT_BY.get());
    }

    @Override
    public void toggleSortBy() {
        Config.SORT_BY.set(getSortBy().next().getSerializedName());
        Config.SORT_BY.save();
    }

    @Override
    public void sendSort(SortRequest request) {
        NetworkHandler.CHANNEL.sendToServer(new ServerSortPacket(request));
    }

    @Override
    public void sendTransfer(TransferRequest request) {
        NetworkHandler.CHANNEL.sendToServer(new ServerTransferPacket(request));
    }

    @Override
    public String getDisableKeyDisplayName() {
        return KeyDisplayNames.displayName(ModKeybindings.DISABLE_KEY);
    }

    @Override
    public ButtonPositions getButtonPositions(String screenType) {
        return Config.getButtonPositions(screenType);
    }

    @Override
    public void saveButtonPositions(String screenType, ButtonPositions positions) {
        Config.saveButtonPositions(screenType, positions);
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
            NetworkHandler.CHANNEL.sendToServer(new com.sighs.sophisticatedsorter.network.ClientOpenContainerSettingsPayload(
                    com.sighs.sophisticatedsorter.settings.ContainerSettingsKey.playerInventory()));
        } else {
            NetworkHandler.CHANNEL.sendToServer(new com.sighs.sophisticatedsorter.network.ClientOpenContainerSettingsPayload());
        }
    }
}
