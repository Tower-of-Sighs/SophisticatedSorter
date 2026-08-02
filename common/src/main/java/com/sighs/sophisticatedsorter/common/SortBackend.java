package com.sighs.sophisticatedsorter.common;

import java.util.List;

/**
 * Minecraft/loader boundary for sorting and transfer operations.
 *
 * <p>The implementation owns all platform objects and the Sophisticated Core
 * storage adapter. {@link SorterService} owns the feature behavior itself.</p>
 */
public interface SortBackend<P, S, I> {
    List<? extends SortSlot<S, I>> containerSlots(P player);

    int inventorySize(P player);

    S inventoryStack(P player, int slotIndex);

    void setInventoryStack(P player, int slotIndex, S stack);

    void sortStacks(List<S> stacks, SortCriterion criterion, boolean pinyinOrder);

    void quickMove(P player, int slotIndex);

    void broadcastChanges(P player);
}
