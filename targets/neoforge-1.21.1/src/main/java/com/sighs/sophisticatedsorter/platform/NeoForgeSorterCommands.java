package com.sighs.sophisticatedsorter.platform;

import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SorterCommands;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsSort;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.server.level.ServerPlayer;

/** NeoForge implementation of the shared command boundary. */
public final class NeoForgeSorterCommands implements SorterCommands<ServerPlayer> {
    public static final NeoForgeSorterCommands INSTANCE = new NeoForgeSorterCommands();

    private NeoForgeSorterCommands() {
    }

    @Override
    public void sort(ServerPlayer player, SortRequest request) {
        if (!ContainerSettingsSort.trySortSettingsAware(player, request)) {
            CoreUtils.executeSort(player, request);
        }
    }

    @Override
    public void transfer(ServerPlayer player, TransferRequest request) {
        CoreUtils.executeTransfer(player, request);
    }
}
