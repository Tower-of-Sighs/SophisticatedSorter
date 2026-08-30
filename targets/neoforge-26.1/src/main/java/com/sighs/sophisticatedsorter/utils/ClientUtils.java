package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.common.SortCriterion;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.SortRequestFactory;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public final class ClientUtils {
    private static volatile ClientPlatform platform;
    private static Class<?> searchBoxClass;

    static {
        try {
            searchBoxClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox");
        } catch (ClassNotFoundException ignored) {
        }
    }

    private ClientUtils() {
    }

    public static void installPlatform(ClientPlatform clientPlatform) {
        if (clientPlatform == null) {
            throw new IllegalArgumentException("clientPlatform");
        }
        platform = clientPlatform;
    }

    public static boolean isDisabledScreen(Screen screen) {
        try {
            return platform().isScreenDisabled(getScreenId(screen));
        } catch (Exception ignored) {
            return true;
        }
    }

    public static String getScreenId(Screen screen) {
        return getTranslationKey(screen.getTitle());
    }

    private static String getTranslationKey(Component component) {
        ComponentContents contents = component.getContents();
        return contents instanceof TranslatableContents translatable ? translatable.getKey() : null;
    }

    public static boolean isZhLang() {
        return platform().isPinyinEnabled()
                && Minecraft.getInstance().getLanguageManager().getSelected().contains("zh_");
    }

    public static boolean isValidScreen() {
        if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen)
                || screen instanceof InventoryScreen) {
            return false;
        }
        AbstractContainerMenu menu = screen.getMenu();
        boolean filter1 = platform().isFilter1Enabled() && menu.slots.size() <= 46;
        boolean filter2 = false;
        if (platform().isFilter2Enabled()) {
            for (Slot slot : menu.slots) {
                if (CoreUtils.isSlotInvalid(slot)) {
                    filter2 = true;
                    break;
                }
            }
        }
        return !isDisabledScreen(screen) && !filter1 && !filter2;
    }

    public static void serverSort() {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
            SortBy sortBy = getSortBy();
            SortRequest request = SortRequestFactory.forScreen(
                    isValidScreen(), SortCriterion.fromWireName(sortBy.getSerializedName()), isZhLang());
            platform().sendSort(request);
        }
    }

    public static void serverTransfer(boolean transferToContainer, boolean filter) {
        platform().sendTransfer(new TransferRequest(transferToContainer, filter));
    }

    public static SortBy getSortBy() {
        return platform().getSortBy();
    }

    public static void toggleSortBy() {
        platform().toggleSortBy();
    }

    /** Creates Core's search control without requiring a StorageScreenBase owner. */
    public static TextBox createSearchBox(Position position, Dimension dimension) {
        if (searchBoxClass == null) {
            return null;
        }
        try {
            Constructor<?> constructor = searchBoxClass.getDeclaredConstructor(
                    Position.class, Dimension.class, StorageScreenBase.class);
            constructor.setAccessible(true);
            return (TextBox) constructor.newInstance(position, dimension, null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    /**
     * Matches Sophisticated Core's ordinary search syntax: plain terms match
     * translated item names, @ terms match the item namespace and # terms
     * match the complete item tooltip.
     */
    public static Predicate<ItemStack> getStackFilter(String phrase) {
        String trimmed = phrase == null ? "" : phrase.trim();
        if (trimmed.isEmpty()) {
            return stack -> true;
        }
        List<Predicate<ItemStack>> terms = new ArrayList<>();
        for (String rawTerm : trimmed.split(" ")) {
            String term = rawTerm.toLowerCase(Locale.ROOT);
            if (term.startsWith("@")) {
                String namespace = term.substring(1);
                terms.add(stack -> !stack.isEmpty()
                        && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()
                        .toLowerCase(Locale.ROOT).contains(namespace));
            } else if (term.startsWith("#")) {
                String tooltipTerm = term.substring(1);
                terms.add(stack -> {
                    if (stack.isEmpty()) {
                        return false;
                    }
                    try {
                        return Screen.getTooltipFromItem(Minecraft.getInstance(), stack).stream()
                                .anyMatch(line -> line.getString().toLowerCase(Locale.ROOT).contains(tooltipTerm));
                    } catch (RuntimeException ignored) {
                        return false;
                    }
                });
            } else {
                terms.add(stack -> !stack.isEmpty()
                        && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(term));
            }
        }
        return stack -> terms.stream().allMatch(predicate -> predicate.test(stack));
    }

    private static ClientPlatform platform() {
        ClientPlatform current = platform;
        if (current == null) {
            throw new IllegalStateException("ClientUtils platform has not been installed");
        }
        return current;
    }
}
