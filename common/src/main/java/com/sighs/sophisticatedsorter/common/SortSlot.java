package com.sighs.sophisticatedsorter.common;

/**
 * A loader-neutral view of one menu slot. The target supplies the Minecraft
 * slot implementation; the sorter only needs these facts to make decisions.
 */
public interface SortSlot<S, I> {
    int index();

    boolean isQuickcraftSlot();

    boolean isInvalid();

    boolean isPlayerInventorySlot();

    S stack();

    void setStack(S stack);

    I item();
}
