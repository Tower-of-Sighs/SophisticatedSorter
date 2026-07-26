package com.sighs.sophisticatedsorter.common;

/** Platform boundary: targets adapt their player/menu objects behind this contract. */
public interface SorterCommands<P> {
    void sort(P player, SortRequest request);
    void transfer(P player, TransferRequest request);
}
