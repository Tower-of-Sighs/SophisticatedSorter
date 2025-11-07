package com.sighs.sophisticatedsorter.utils;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.inventory.IItemHandlerSimpleInserter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SimpleSlottedStackStorage implements IItemHandlerSimpleInserter {
    private final List<ItemStack> stacks;
    private final int slotLimit;
    private final List<SingleSlotStorage<ItemVariant>> slotStorages;

    public SimpleSlottedStackStorage(int size) {
        this(size, 64);
    }

    public SimpleSlottedStackStorage(int size, int slotLimit) {
        this.stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.stacks.add(ItemStack.EMPTY);
        }
        this.slotLimit = slotLimit;
        this.slotStorages = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.slotStorages.add(new SimpleSingleSlotStorage(i));
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return stacks.get(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        stacks.set(slot, stack == null ? ItemStack.EMPTY : stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotLimit;
    }

    @Override
    public int getSlotCount() {
        return stacks.size();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return slotStorages.get(slot);
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return Collections.unmodifiableList(slotStorages);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    private class SlotView implements StorageView<ItemVariant> {
        private final int index;

        private SlotView(int index) {
            this.index = index;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return getSlot(index).extract(resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return stacks.get(index).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(stacks.get(index));
        }

        @Override
        public long getAmount() {
            return stacks.get(index).getCount();
        }

        @Override
        public long getCapacity() {
            return getSlotLimit(index);
        }

        @Override
        public StorageView<ItemVariant> getUnderlyingView() {
            return this;
        }
    }

    private class SimpleSingleSlotStorage implements SingleSlotStorage<ItemVariant> {
        private final int index;

        private SimpleSingleSlotStorage(int index) {
            this.index = index;
        }

        @Override
        public boolean isResourceBlank() {
            return stacks.get(index).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(stacks.get(index));
        }

        @Override
        public long getAmount() {
            return stacks.get(index).getCount();
        }

        @Override
        public long getCapacity() {
            return getSlotLimit(index);
        }

        @Override
        public StorageView<ItemVariant> getUnderlyingView() {
            return this;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            ItemStack current = stacks.get(index);
            int existing = current.getCount();
            int limit = getSlotLimit(index);

            if (!current.isEmpty() && !resource.matches(current)) {
                return 0;
            }

            long canInsert = Math.min(Math.max(limit - existing, 0), Math.max(maxAmount, 0));
            if (canInsert <= 0) {
                return 0;
            }

            ItemStack newStack = resource.toStack(existing + (int) canInsert);
            stacks.set(index, newStack);
            return canInsert;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            ItemStack current = stacks.get(index);
            if (current.isEmpty() || !resource.matches(current)) {
                return 0;
            }

            long extracted = Math.min(current.getCount(), Math.max(maxAmount, 0));
            int remaining = current.getCount() - (int) extracted;

            if (remaining <= 0) {
                stacks.set(index, ItemStack.EMPTY);
            } else {
                ItemStack newStack = current.copy();
                newStack.setCount(remaining);
                stacks.set(index, newStack);
            }
            return extracted;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return Collections.<StorageView<ItemVariant>>singletonList(new SlotView(index)).iterator();
        }
    }
}