package com.sighs.sophisticatedsorter.common;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Platform-neutral lifecycle policy for sorter controls attached to a
 * container screen. Targets supply only Minecraft/Core values and widgets.
 */
public final class ContainerScreenBehavior<T> {
    private final ContainerScreenController<T> controller = new ContainerScreenController<T>();

    public boolean initialize(boolean creativeScreen, boolean validScreen,
                              boolean inventoryScreen, boolean storageScreen) {
        return controller.initialize(creativeScreen, validScreen, inventoryScreen, storageScreen);
    }

    public boolean isInventoryScreen() {
        return controller.isInventoryScreen();
    }

    public void updateSearch(String searchPhrase, Function<String, Predicate<T>> filterFactory) {
        controller.updateSearch(searchPhrase, filterFactory);
    }

    public boolean shouldReinitialize(boolean validScreen) {
        return controller.shouldReinitialize(validScreen);
    }

    public boolean isFiltered(T value) {
        return controller.isFiltered(value);
    }

    public boolean shouldHandleTooltip(boolean creativeScreen) {
        return !creativeScreen;
    }

    public boolean shouldHandleClick(boolean creativeScreen, boolean storageScreen) {
        return !creativeScreen && !storageScreen;
    }

    public boolean shouldHandleTick(boolean creativeScreen) {
        return !creativeScreen;
    }

    public boolean shouldHandleSlotRender(boolean creativeScreen) {
        return !creativeScreen;
    }

    public ContainerScreenLayout.Positions positions(
            int left,
            int top,
            int imageWidth,
            int inventoryLabelX,
            int inventoryLabelY,
            int inventoryRight,
            int inventoryTop) {
        return ContainerScreenLayout.positions(left, top, imageWidth, inventoryLabelX,
                inventoryLabelY, controller.isInventoryScreen(), inventoryRight, inventoryTop);
    }

    public int maxInventoryX(int current, int slotX) {
        return ContainerScreenLayout.maxInventoryX(current, slotX);
    }

    public int minInventoryY(int current, int slotY) {
        return ContainerScreenLayout.minInventoryY(current, slotY);
    }
}
