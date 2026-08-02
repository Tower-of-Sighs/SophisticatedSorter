package com.sighs.sophisticatedsorter.common;

import java.util.ArrayList;
import java.util.List;

/** Shared sorting and transfer behavior for every loader/version target. */
public final class SorterService<P, S, I> {
    private final SortBackend<P, S, I> backend;

    public SorterService(SortBackend<P, S, I> backend) {
        this.backend = backend;
    }

    public void sort(P player, SortRequest request) {
        if (request == null || request.target() == SortTarget.INVENTORY) {
            sortInventory(player, request == null
                    ? new SortRequest(SortCriterion.NAME, SortTarget.INVENTORY, false)
                    : request);
            return;
        }

        List<SortSlot<S, I>> sortableSlots = new ArrayList<SortSlot<S, I>>();
        for (SortSlot<S, I> slot : backend.containerSlots(player)) {
            if (!slot.isQuickcraftSlot() && !slot.isInvalid() && !slot.isPlayerInventorySlot()) {
                sortableSlots.add(slot);
            }
        }

        List<S> stacks = new ArrayList<S>(sortableSlots.size());
        for (SortSlot<S, I> slot : sortableSlots) {
            stacks.add(slot.stack());
        }
        backend.sortStacks(stacks, request.criterion(), request.pinyinOrder());
        for (int index = 0; index < sortableSlots.size(); index++) {
            sortableSlots.get(index).setStack(stacks.get(index));
        }
        backend.broadcastChanges(player);
    }

    public void transfer(P player, TransferRequest request) {
        if (request == null) {
            return;
        }

        boolean toContainer = request.toContainer();
        boolean filterByDestination = request.filterByDestination();
        List<SortSlot<S, I>> transferSlots = new ArrayList<SortSlot<S, I>>();
        List<I> destinationItems = new ArrayList<I>();
        for (SortSlot<S, I> slot : backend.containerSlots(player)) {
            if (toContainer == slot.isPlayerInventorySlot()) {
                transferSlots.add(slot);
            } else {
                destinationItems.add(slot.item());
            }
        }
        if (toContainer) {
            transferSlots = ContainerSelection.withoutTrailingHotbar(transferSlots, 9);
        }
        for (SortSlot<S, I> slot : transferSlots) {
            if (!filterByDestination || destinationItems.contains(slot.item())) {
                backend.quickMove(player, slot.index());
            }
        }
        backend.broadcastChanges(player);
    }

    private void sortInventory(P player, SortRequest request) {
        List<Integer> sortableSlots = ContainerSelection.playerMainInventorySlots(backend.inventorySize(player));
        List<S> stacks = new ArrayList<S>(sortableSlots.size());
        for (Integer slotIndex : sortableSlots) {
            stacks.add(backend.inventoryStack(player, slotIndex));
        }
        backend.sortStacks(stacks, request.criterion(), request.pinyinOrder());
        for (int index = 0; index < sortableSlots.size(); index++) {
            backend.setInventoryStack(player, sortableSlots.get(index), stacks.get(index));
        }
        backend.broadcastChanges(player);
    }
}
