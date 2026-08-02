package com.sighs.sophisticatedsorter.utils;

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
}
