package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.common.PinyinOrdering;
import com.sighs.sophisticatedsorter.common.SortBackend;
import com.sighs.sophisticatedsorter.common.SortComparatorProvider;
import com.sighs.sophisticatedsorter.common.SortComparatorSelection;
import com.sighs.sophisticatedsorter.common.SortCriterion;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SortSlot;
import com.sighs.sophisticatedsorter.common.SorterService;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.mixin.AbstractContainerMenuAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CoreUtils {
    public static final Comparator<Map.Entry<ItemStackKey, Integer>> BY_PINYIN =
            PinyinOrdering.<Map.Entry<ItemStackKey, Integer>>byName(
                    entry -> entry.getKey().stack().getHoverName().getString());
    private static final SortComparatorProvider<Map.Entry<ItemStackKey, Integer>> COMPARATORS =
            SortComparatorProvider.of(InventorySorter.BY_NAME, InventorySorter.BY_MOD,
                    InventorySorter.BY_COUNT, InventorySorter.BY_TAGS, BY_PINYIN);

    private static volatile SortPlatform platform;
    private static volatile SorterService<Player, ItemStack, Item> sorter;

    private CoreUtils() {
    }

    public static void installPlatform(SortPlatform sortPlatform) {
        if (sortPlatform == null) {
            throw new IllegalArgumentException("sortPlatform");
        }
        platform = sortPlatform;
        sorter = new SorterService<>(new MinecraftSortBackend());
    }

    public static Comparator<Map.Entry<ItemStackKey, Integer>> getComparator(SortBy sortBy, boolean pinyin) {
        SortCriterion criterion = sortBy == null
                ? SortCriterion.NAME
                : SortCriterion.fromWireName(sortBy.getSerializedName());
        return SortComparatorSelection.select(criterion, pinyin, COMPARATORS);
    }

    public static boolean isSlotInvalid(Slot slot) {
        return !slot.mayPlace(new ItemStack(Items.BARRIER)) || slot instanceof ResultSlot;
    }

    public static void executeSort(Player player, SortRequest request) {
        sorter().sort(player, request);
    }

    public static void executeTransfer(Player player, TransferRequest request) {
        sorter().transfer(player, request);
    }

    private static SorterService<Player, ItemStack, Item> sorter() {
        SorterService<Player, ItemStack, Item> current = sorter;
        if (current == null) {
            throw new IllegalStateException("CoreUtils platform has not been installed");
        }
        return current;
    }

    private static SortPlatform platform() {
        SortPlatform current = platform;
        if (current == null) {
            throw new IllegalStateException("CoreUtils platform has not been installed");
        }
        return current;
    }

    private static final class MinecraftSortBackend implements SortBackend<Player, ItemStack, Item> {
        @Override
        public List<? extends SortSlot<ItemStack, Item>> containerSlots(Player player) {
            AbstractContainerMenu menu = player.containerMenu;
            Set<Slot> quickcraftSlots = ((AbstractContainerMenuAccessor) menu)
                    .sophisticatedSorter$getQuickcraftSlots();
            List<SortSlot<ItemStack, Item>> slots = new ArrayList<>(menu.slots.size());
            for (Slot slot : menu.slots) {
                slots.add(new MinecraftSortSlot(slot, quickcraftSlots.contains(slot)));
            }
            return slots;
        }

        @Override
        public int inventorySize(Player player) {
            return player.getInventory().getContainerSize();
        }

        @Override
        public ItemStack inventoryStack(Player player, int slotIndex) {
            return player.getInventory().getItem(slotIndex);
        }

        @Override
        public void setInventoryStack(Player player, int slotIndex, ItemStack stack) {
            player.getInventory().setItem(slotIndex, stack);
        }

        @Override
        public void sortStacks(List<ItemStack> stacks, SortCriterion criterion, boolean pinyinOrder) {
            platform().sortStacks(stacks,
                    SortComparatorSelection.select(criterion, pinyinOrder, COMPARATORS));
        }

        @Override
        public void quickMove(Player player, int slotIndex) {
            player.containerMenu.quickMoveStack(player, slotIndex);
        }

        @Override
        public void broadcastChanges(Player player) {
            player.containerMenu.broadcastChanges();
        }
    }

    private record MinecraftSortSlot(Slot slot, boolean quickcraftSlot)
            implements SortSlot<ItemStack, Item> {
        @Override
        public int index() {
            return slot.index;
        }

        @Override
        public boolean isQuickcraftSlot() {
            return quickcraftSlot;
        }

        @Override
        public boolean isInvalid() {
            return CoreUtils.isSlotInvalid(slot);
        }

        @Override
        public boolean isPlayerInventorySlot() {
            return slot.container instanceof Inventory;
        }

        @Override
        public ItemStack stack() {
            return slot.getItem();
        }

        @Override
        public void setStack(ItemStack stack) {
            slot.set(stack);
        }

        @Override
        public Item item() {
            return slot.getItem().getItem();
        }
    }
}
