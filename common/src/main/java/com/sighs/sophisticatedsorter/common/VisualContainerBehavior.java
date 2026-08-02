package com.sighs.sophisticatedsorter.common;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Platform-neutral behavior of the transient menu used while sorting or
 * transferring.  Targets only adapt the Core/Minecraft return types.
 */
public final class VisualContainerBehavior<Position, Entity, UpgradeSlot, Stack> {
    private final Supplier<Stack> emptyStackFactory;

    public VisualContainerBehavior(Supplier<Stack> emptyStackFactory) {
        this.emptyStackFactory = emptyStackFactory;
    }

    public Optional<Position> blockPosition() {
        return Optional.empty();
    }

    public Optional<Entity> entity() {
        return Optional.empty();
    }

    public UpgradeSlot upgradeSlot() {
        return null;
    }

    public void openSettings() {
    }

    public boolean storageItemHasChanged() {
        return false;
    }

    public boolean detectSettingsChangeAndReload() {
        return false;
    }

    public boolean stillValid() {
        return false;
    }

    public int noSlotCount() {
        return 0;
    }

    public Set<Integer> noSortSlotIndexes() {
        return Collections.emptySet();
    }

    public boolean shouldSuppressSlotPopulation(boolean visualMenu) {
        return visualMenu;
    }

    public Stack emptyQuickMove() {
        return emptyStackFactory.get();
    }
}
