package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.Config;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import com.sighs.sophisticatedsorter.network.ServerSortPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.items.ItemStackHandler;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;

import java.util.*;

public class CoreUtils {
    public static SortBy getSortBy() {
        return SortBy.fromName(Config.SORT_BY.get());
    }
    public static void toggleSortBy() {
        Config.SORT_BY.set(getSortBy().next().getSerializedName());
        Config.SORT_BY.save();
    }

    public static Comparator<Map.Entry<ItemStackKey, Integer>> getComparator(SortBy sortBy) {
        return switch (sortBy) {
            case COUNT -> InventorySorter.BY_COUNT;
            case TAGS -> InventorySorter.BY_TAGS;
            case NAME -> InventorySorter.BY_NAME;
            case MOD -> InventorySorter.BY_MOD;
        };
    }

    public static void serverSort() {
        String target = "container";
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        boolean filter = Config.Filter.get() && player.containerMenu.slots.size() <= 46;
        if (player.containerMenu instanceof InventoryMenu || filter) target = "inventory";
        NetworkHandler.CHANNEL.sendToServer(new ServerSortPacket(CoreUtils.getSortBy().name(), target));
    }

    public static void sortContainer(ServerPlayer player, SortBy sortBy) {
        var menu = player.containerMenu;
        var items = menu.getItems();
        List<Integer> needSort = new ArrayList<>();

        for (int i = 0; i < items.size() - 36; i++) needSort.add(i);

        var handler = new ItemStackHandler(needSort.size());
        for (int i = 0; i < needSort.size(); i++) {
            handler.setStackInSlot(i, items.get(needSort.get(i)));
        }

        InventorySorter.sortHandler(handler, CoreUtils.getComparator(sortBy), new HashSet<>());

        for (int i = 0; i < needSort.size(); i++) {
            menu.getSlot(needSort.get(i)).set(handler.getStackInSlot(i));
        }
    }

    public static void sortInventory(ServerPlayer player, SortBy sortBy) {
        Inventory inventory = player.getInventory();
        var items = inventory.items;
        List<Integer> needSort = new ArrayList<>();

        for (int i = 9; i < 36; i++) needSort.add(i);

        var handler = new ItemStackHandler(needSort.size());
        for (int i = 0; i < needSort.size(); i++) {
            handler.setStackInSlot(i, items.get(needSort.get(i)));
        }

        InventorySorter.sortHandler(handler, CoreUtils.getComparator(sortBy), new HashSet<>());

        for (int i = 0; i < needSort.size(); i++) {
            inventory.setItem(needSort.get(i), handler.getStackInSlot(i));
        }
    }
}
