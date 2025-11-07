package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.ModConfig;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.network.ServerSortPacket;
import com.sighs.sophisticatedsorter.network.ServerTransferPacket;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

import java.text.Collator;
import java.util.*;

public class CoreUtils {
    public static final Comparator<Map.Entry<ItemStackKey, Integer>> BY_PINYIN =
            Comparator.comparing((Map.Entry<ItemStackKey, Integer> o) ->
                            o.getKey().getStack().getHoverName().getString(),
                    // 使用中文Collator进行拼音排序
                    Collator.getInstance(Locale.CHINA)
            );

    public static SortBy getSortBy() {
        return ModConfig.INSTANCE.SORT_BY;
    }

    public static void toggleSortBy() {
        ModConfig.INSTANCE.SORT_BY = ModConfig.INSTANCE.SORT_BY.next();
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }


    public static Comparator<Map.Entry<ItemStackKey, Integer>> getComparator(SortBy sortBy, boolean zh) {
        Comparator<Map.Entry<ItemStackKey, Integer>> comparator = switch (sortBy) {
            case COUNT -> InventorySorter.BY_COUNT;
            case TAGS -> InventorySorter.BY_TAGS;
            case NAME -> zh ? BY_PINYIN : InventorySorter.BY_NAME;
            case MOD -> InventorySorter.BY_MOD;
        };
        if (zh) comparator = comparator.thenComparing(BY_PINYIN);
        return comparator;
    }

    public static boolean canContainerSort(AbstractContainerMenu menu) {
        if (menu instanceof InventoryMenu) return false;
        boolean filter = ModConfig.INSTANCE.FILTER && menu.slots.size() <= 46;
        return !filter;
    }

    public static void serverSort(AbstractContainerMenu menu) {
        String target = "container";
        if (!canContainerSort(menu)) target = "inventory";
        NetworkHandler.sendToServer(new ServerSortPacket(getSortBy().getSerializedName(), target, ClientUtils.isZhLang()));
    }

    public static void serverTransfer(boolean transferToContainer, boolean filter) {
        NetworkHandler.sendToServer(new ServerTransferPacket(transferToContainer, filter));
    }

    public static boolean isSlotInvalid(Slot slot) {
        return !slot.mayPlace(ItemStack.EMPTY) || slot instanceof ResultSlot;
    }

    public static void sortContainer(ServerPlayer player, SortBy sortBy, boolean zh) {
        var menu = player.containerMenu;
        List<Integer> needSort = new ArrayList<>();

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.getSlot(i);
            if (menu.quickcraftSlots.contains(slot) || isSlotInvalid(slot)) continue;
            if (!(slot.container instanceof Inventory)) needSort.add(i);
        }

        var handler = new SimpleSlottedStackStorage(needSort.size());
        for (int i = 0; i < needSort.size(); i++) {
            handler.setStackInSlot(i, menu.getSlot(needSort.get(i)).getItem());
        }

        InventorySorter.sortHandler(handler, CoreUtils.getComparator(sortBy, zh), new HashSet<>());

        for (int i = 0; i < needSort.size(); i++) {
            menu.getSlot(needSort.get(i)).set(handler.getStackInSlot(i));
        }
    }

    public static void sortInventory(ServerPlayer player, SortBy sortBy, boolean zh) {
        Inventory inventory = player.getInventory();
        var items = inventory.items;
        List<Integer> needSort = new ArrayList<>();

        for (int i = 9; i < 36; i++) needSort.add(i);

        var handler = new SimpleSlottedStackStorage(needSort.size());
        for (int i = 0; i < needSort.size(); i++) {
            handler.setStackInSlot(i, items.get(needSort.get(i)));
        }

        InventorySorter.sortHandler(handler, CoreUtils.getComparator(sortBy, zh), new HashSet<>());

        for (int i = 0; i < needSort.size(); i++) {
            inventory.setItem(needSort.get(i), handler.getStackInSlot(i));
        }
    }

    public static void transfer(Player player, boolean transferToContainer, boolean filter) {
        AbstractContainerMenu menu = player.containerMenu;
        List<Slot> needTransferSlots = new ArrayList<>();
        List<Item> targetSlotItems = new ArrayList<>();
        for (Slot slot : menu.slots) {
            // 要转移到容器且为物品栏槽位时，或，并非转移到容器且不为物品栏槽位时。
            if (transferToContainer == slot.container instanceof Inventory) {
                needTransferSlots.add(slot);
            } else targetSlotItems.add(slot.getItem().getItem());
        }
        if (transferToContainer) {
            // 去掉玩家快捷栏。
            needTransferSlots.subList(needTransferSlots.size() - 9, needTransferSlots.size()).clear();
        }
        for (Slot slot : needTransferSlots) {
            // 不筛选就转移，筛选了就看一下有没有在目标容器里再转移。
            if (!filter || targetSlotItems.contains(slot.getItem().getItem())) {
                menu.quickMoveStack(player, slot.index);
            }
        }
    }
}
