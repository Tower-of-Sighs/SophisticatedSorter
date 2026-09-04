package com.sighs.sophisticatedsorter.platform;

import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SorterCommands;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsSort;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.server.level.ServerPlayer;

/** Fabric implementation of the loader-neutral command contract. */
public final class FabricSorterCommands implements SorterCommands<ServerPlayer> {
    public static final FabricSorterCommands INSTANCE = new FabricSorterCommands();

    private FabricSorterCommands() {
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
