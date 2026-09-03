package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.common.ButtonPositions;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;

/** Configuration and packet boundary used by the shared client utility. */
public interface ClientPlatform {
    boolean isScreenDisabled(String screenId);

    boolean isFilter1Enabled();

    boolean isFilter2Enabled();

    boolean isPinyinEnabled();

    SortBy getSortBy();

    void toggleSortBy();

    void sendSort(SortRequest request);

    void sendTransfer(TransferRequest request);

    String getDisableKeyDisplayName();

    ButtonPositions getButtonPositions(String screenType);

    void saveButtonPositions(String screenType, ButtonPositions positions);

    /**
     * Whether this target provides a per-container settings screen reachable from the top-right
     * button group. When true the shared screen mixin adds a fourth (settings) button to the group
     * and shifts the group left by one button slot.
     */
    default boolean hasContainerSettings() {
        return false;
    }

    /**
     * Called when the settings button in the top-right button group is clicked.
     *
     * @param playerInventoryScreen whether the current screen is the player-inventory main screen
     *                              (the settings target is then the player inventory)
     */
    default void openSettingsRequested(boolean playerInventoryScreen) {
    }
}
