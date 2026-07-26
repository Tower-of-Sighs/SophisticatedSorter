package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.Config;
import com.sighs.sophisticatedsorter.api.IStorageScreenBase;
import com.sighs.sophisticatedsorter.network.ServerSortPacket;
import com.sighs.sophisticatedsorter.network.ServerTransferPacket;
import com.sighs.sophisticatedsorter.visual.VisualStorageScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientUtils {
    private static final Logger log = LogManager.getLogger(ClientUtils.class);
    private static Class<?> searchBoxClass;
    private static Class<?> transferButton;

    static {
        try {
            searchBoxClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox");
            transferButton = Class.forName("net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase$TransferButton");
        } catch (ClassNotFoundException ignored) {}
    }

    public static TextBox createSearchBox(Object... params) {
        if (searchBoxClass != null) {
            try {
                Constructor<?> constructor = searchBoxClass.getDeclaredConstructor(Position.class, Dimension.class, StorageScreenBase.class);
                constructor.setAccessible(true);
                return (TextBox) constructor.newInstance(params);
            } catch (Exception ignored) {}
        }
        return null;
    }
    public static Button createTransferButton(Object... params) {
        if (transferButton != null) {
            try {
                Constructor<?> constructor = transferButton.getDeclaredConstructor(StorageScreenBase.class, Consumer.class, ButtonDefinition.class, ButtonDefinition.class);
                constructor.setAccessible(true);
                return (Button) constructor.newInstance(params);
            } catch (Exception e) {
                log.error("e: ", e);
            }
        }
        return null;
    }

    public static Predicate<ItemStack> getStackFilter(String string) {
        var faker = new VisualStorageScreen();
        return ((IStorageScreenBase) faker).getVisualStackFilter(string);
    }

    public static boolean isDisabledScreen(Screen screen) {
        boolean result = true;
        try {
            result = Config.BLACKLIST.get().contains(getScreenId(screen));
        } catch (Exception ignored) {}
        return result;
    }
    public static String getScreenId(Screen screen) {
        return getTranslationKey(screen.getTitle());
    }
    private static String getTranslationKey(Component component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        return null;
    }

    public static boolean isZhLang() {
        if (!Config.PINYIN.get()) return false;
        return Minecraft.getInstance().getLanguageManager().getSelected().contains("zh_");
    }

    public static void serverSort() {
        String target = "container";
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            if (screen instanceof StorageScreenBase) return;
            if (!isValidScreen()) target = "inventory";
            PacketDistributor.sendToServer(new ServerSortPacket(getSortBy().getSerializedName(), target, ClientUtils.isZhLang()));
        }
    }
    public static void serverTransfer(boolean transferToContainer, boolean filter) {
        PacketDistributor.sendToServer(new ServerTransferPacket(transferToContainer, filter));
    }

    public static boolean isValidScreen() {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            if (screen instanceof InventoryScreen) return false;
            AbstractContainerMenu menu = screen.getMenu();
            boolean filter1 = Config.FILTER1.get() && menu.slots.size() <= 46;
            boolean filter2 = false;
            if (Config.FILTER2.get()) for (Slot slot : menu.slots) {
                if (CoreUtils.isSlotInvalid(slot)) {
                    filter2 = true;
                    break;
                }
            }
            return !isDisabledScreen(screen) && !filter1 && !filter2;
        }
        return false;
    }

    public static SortBy getSortBy() {
        return SortBy.fromName(Config.SORT_BY.get());
    }
    public static void toggleSortBy() {
        Config.SORT_BY.set(getSortBy().next().getSerializedName());
        Config.SORT_BY.save();
    }
}
