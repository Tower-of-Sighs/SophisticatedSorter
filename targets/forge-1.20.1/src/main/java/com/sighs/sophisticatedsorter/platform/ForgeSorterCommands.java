package com.sighs.sophisticatedsorter.platform;

import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SorterCommands;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.server.level.ServerPlayer;

/** The only Forge binding for the common command contract. */
public final class ForgeSorterCommands implements SorterCommands<ServerPlayer> {
    public static final ForgeSorterCommands INSTANCE = new ForgeSorterCommands();

    private ForgeSorterCommands() {
    }

    @Override
    public void sort(ServerPlayer player, SortRequest request) {
        CoreUtils.executeSort(player, request);
    }

    @Override
    public void transfer(ServerPlayer player, TransferRequest request) {
        CoreUtils.executeTransfer(player, request);
    }
}
