package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.api.IStorageScreenBase;
import com.sighs.sophisticatedsorter.common.ButtonPositions;
import com.sighs.sophisticatedsorter.common.ScreenId;
import com.sighs.sophisticatedsorter.common.SortCriterion;
import com.sighs.sophisticatedsorter.common.SortRequestFactory;
import com.sighs.sophisticatedsorter.common.SortRequest;
import com.sighs.sophisticatedsorter.common.TransferRequest;
import com.sighs.sophisticatedsorter.visual.VisualStorageScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Shared screen logic; configuration and packet transport are target adapters. */
public final class ClientUtils {
    private static final Logger LOG = LogManager.getLogger(ClientUtils.class);
    private static volatile ClientPlatform platform;
    private static Class<?> searchBoxClass;
    private static Class<?> transferButton;

    static {
        try {
            searchBoxClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox");
            transferButton = Class.forName("net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase$TransferButton");
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

    public static TextBox createSearchBox(Object... params) {
        if (searchBoxClass == null) {
            return null;
        }
        try {
            Constructor<?> constructor = searchBoxClass.getDeclaredConstructor(
                    Position.class, Dimension.class, StorageScreenBase.class);
            constructor.setAccessible(true);
            return (TextBox) constructor.newInstance(params);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Button createTransferButton(Object... params) {
        if (transferButton == null) {
            return null;
        }
        try {
            Constructor<?> constructor = transferButton.getDeclaredConstructor(
                    StorageScreenBase.class, Consumer.class, ButtonDefinition.class, ButtonDefinition.class);
            constructor.setAccessible(true);
            return (Button) constructor.newInstance(params);
        } catch (Exception exception) {
            LOG.error("Could not create transfer button", exception);
            return null;
        }
    }

    public static Predicate<ItemStack> getStackFilter(String string) {
        VisualStorageScreen visualScreen = new VisualStorageScreen();
        return ((IStorageScreenBase) visualScreen).getVisualStackFilter(string);
    }

    public static boolean isDisabledScreen(Screen screen) {
        try {
            return platform().isScreenDisabled(getScreenId(screen));
        } catch (Exception ignored) {
            return true;
        }
    }

    /**
     * Stable identifier for one screen for the per-screen client options (button visibility toggle and
     * button offsets): {@code "<screen class>@<title translation key>"}. The screen class alone would
     * conflate screens that share one class (vanilla chests, barrels, shulker boxes and trapped chests
     * all use {@link net.minecraft.client.gui.screens.inventory.ChestScreen}), and the title key alone
     * would conflate different screen classes that show the same translated title, so both are part of
     * the key. Screens whose title is not translatable (no key) are identified by the class name only.
     */
    public static String getScreenId(Screen screen) {
        return ScreenId.build(screen.getClass().getName(), getTranslationKey(screen.getTitle()));
    }

    private static String getTranslationKey(Component component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents) {
            return ((TranslatableContents) contents).getKey();
        }
        return null;
    }

    public static boolean isZhLang() {
        return platform().isPinyinEnabled()
                && Minecraft.getInstance().getLanguageManager().getSelected().contains("zh_");
    }

    public static boolean isValidScreen() {
        if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen)) {
            return false;
        }
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) Minecraft.getInstance().screen;
        if (screen instanceof InventoryScreen) {
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
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
            SortBy sortBy = getSortBy();
            SortRequest request = SortRequestFactory.forScreen(
                    isValidScreen(),
                    SortCriterion.fromWireName(sortBy.getSerializedName()),
                    isZhLang());
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

    public static String disableKeyDisplayName() {
        return platform().getDisableKeyDisplayName();
    }

    public static ButtonPositions getButtonPositions(String screenType) {
        return platform().getButtonPositions(screenType);
    }

    public static void saveButtonPositions(String screenType, ButtonPositions positions) {
        platform().saveButtonPositions(screenType, positions);
    }

    public static boolean hasContainerSettings() {
        return platform().hasContainerSettings();
    }

    public static void openSettingsRequested(boolean playerInventoryScreen) {
        platform().openSettingsRequested(playerInventoryScreen);
    }

    private static ClientPlatform platform() {
        ClientPlatform current = platform;
        if (current == null) {
            throw new IllegalStateException("ClientUtils platform has not been installed");
        }
        return current;
    }
}
