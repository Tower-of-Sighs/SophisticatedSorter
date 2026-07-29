package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.common.ContainerSelection;
import com.sighs.sophisticatedsorter.common.PinyinOrdering;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SortTarget;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** Minecraft/Forge adapter. Command validation and menu-independent rules live in common. */
public final class CoreUtils {
    private static final ThreadLocal<Boolean> LIMIT_TO_ITEM_MAX_STACK_SIZE = ThreadLocal.withInitial(() -> false);

    public static final Comparator<Map.Entry<ItemStackKey, Integer>> BY_PINYIN =
            PinyinOrdering.<Map.Entry<ItemStackKey, Integer>>byName(
                    entry -> entry.getKey().getStack().getHoverName().getString());

    private CoreUtils() {
    }

    public static Comparator<Map.Entry<ItemStackKey, Integer>> getComparator(SortBy sortBy, boolean zh) {
        Comparator<Map.Entry<ItemStackKey, Integer>> comparator = switch (sortBy) {
            case COUNT -> InventorySorter.BY_COUNT;
            case TAGS -> InventorySorter.BY_TAGS;
            case NAME -> zh ? BY_PINYIN : InventorySorter.BY_NAME;
            case MOD -> InventorySorter.BY_MOD;
        };
        return zh ? comparator.thenComparing(BY_PINYIN) : comparator;
    }

    public static boolean isSlotInvalid(Slot slot) {
        return !slot.mayPlace(new ItemStack(Items.BARRIER)) || slot instanceof ResultSlot;
    }

    public static boolean shouldLimitToItemMaxStackSize() {
        return LIMIT_TO_ITEM_MAX_STACK_SIZE.get();
    }

    public static void executeSort(Player player, SortRequest request) {
        SortBy sortBy = SortBy.fromName(request.criterion().wireName());
        if (request.target() == SortTarget.INVENTORY) {
            sortInventory(player, sortBy, request.pinyinOrder());
        } else {
            sortContainer(player, sortBy, request.pinyinOrder());
        }
    }

    public static void executeTransfer(Player player, TransferRequest request) {
        transfer(player, request.toContainer(), request.filterByDestination());
    }

    private static void sortContainer(Player player, SortBy sortBy, boolean zh) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Integer> sortableSlots = new ArrayList<Integer>();
        for (int index = 0; index < menu.slots.size(); index++) {
            Slot slot = menu.getSlot(index);
            if (!menu.quickcraftSlots.contains(slot) && !isSlotInvalid(slot) && !(slot.container instanceof Inventory)) {
                sortableSlots.add(index);
            }
        }
        ItemStackHandler handler = new ItemStackHandler(sortableSlots.size());
        for (int index = 0; index < sortableSlots.size(); index++) {
            handler.setStackInSlot(index, menu.getSlot(sortableSlots.get(index)).getItem());
        }
        sortHandler(handler, sortBy, zh);
        for (int index = 0; index < sortableSlots.size(); index++) {
            menu.getSlot(sortableSlots.get(index)).set(handler.getStackInSlot(index));
        }
        menu.broadcastChanges();
    }

    private static void sortInventory(Player player, SortBy sortBy, boolean zh) {
        Inventory inventory = player.getInventory();
        List<Integer> sortableSlots = ContainerSelection.playerMainInventorySlots(inventory.items.size());
        ItemStackHandler handler = new ItemStackHandler(sortableSlots.size());
        for (int index = 0; index < sortableSlots.size(); index++) {
            handler.setStackInSlot(index, inventory.items.get(sortableSlots.get(index)));
        }
        sortHandler(handler, sortBy, zh);
        for (int index = 0; index < sortableSlots.size(); index++) {
            inventory.setItem(sortableSlots.get(index), handler.getStackInSlot(index));
        }
        player.inventoryMenu.broadcastChanges();
    }

    private static void sortHandler(ItemStackHandler handler, SortBy sortBy, boolean zh) {
        LIMIT_TO_ITEM_MAX_STACK_SIZE.set(true);
        try {
            InventorySorter.sortHandler(handler, getComparator(sortBy, zh), new HashSet<Integer>());
        } finally {
            LIMIT_TO_ITEM_MAX_STACK_SIZE.remove();
        }
    }

    private static void transfer(Player player, boolean transferToContainer, boolean filter) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> transferSlots = new ArrayList<Slot>();
        List<Item> targetItems = new ArrayList<Item>();
        for (Slot slot : menu.slots) {
            if (transferToContainer == slot.container instanceof Inventory) {
                transferSlots.add(slot);
            } else {
                targetItems.add(slot.getItem().getItem());
            }
        }
        if (transferToContainer) {
            transferSlots = new ArrayList<Slot>(ContainerSelection.withoutTrailingHotbar(transferSlots, 9));
        }
        for (Slot slot : transferSlots) {
            if (!filter || targetItems.contains(slot.getItem().getItem())) {
                menu.quickMoveStack(player, slot.index);
            }
        }
        menu.broadcastChanges();
    }
}
