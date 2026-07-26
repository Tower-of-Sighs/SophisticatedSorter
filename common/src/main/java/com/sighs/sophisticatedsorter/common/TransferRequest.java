package com.sighs.sophisticatedsorter.common;

/** Loader-neutral transfer command. */
public final class TransferRequest {
    private final boolean toContainer;
    private final boolean filterByDestination;

    public TransferRequest(boolean toContainer, boolean filterByDestination) {
        this.toContainer = toContainer;
        this.filterByDestination = filterByDestination;
    }

    public boolean toContainer() { return toContainer; }
    public boolean filterByDestination() { return filterByDestination; }
}
