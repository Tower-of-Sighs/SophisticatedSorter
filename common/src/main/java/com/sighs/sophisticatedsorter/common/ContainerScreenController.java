package com.sighs.sophisticatedsorter.common;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Platform-neutral state machine for the sorter controls attached to a
 * container screen.  The target supplies only screen and item conversions.
 */
public final class ContainerScreenController<T> {
    private boolean screenDisabled = true;
    private boolean inventoryScreen;
    private Predicate<T> stackPredicate;

    /**
     * Applies the exact initialization gates used by the original screen
     * bridge.  A false result means the target must stop adding controls.
     */
    public boolean initialize(boolean creativeScreen, boolean validScreen,
                             boolean inventoryScreen, boolean storageScreen) {
        if (creativeScreen) {
            return false;
        }
        screenDisabled = !validScreen;
        if (storageScreen) {
            return false;
        }
        this.inventoryScreen = inventoryScreen;
        return inventoryScreen || !screenDisabled;
    }

    public boolean shouldReinitialize(boolean validScreen) {
        return screenDisabled == validScreen;
    }

    public boolean isInventoryScreen() {
        return inventoryScreen;
    }

    public void updateSearch(String searchPhrase, Function<String, Predicate<T>> filterFactory) {
        if (searchPhrase == null || searchPhrase.isEmpty()) {
            stackPredicate = null;
        } else {
            stackPredicate = filterFactory.apply(searchPhrase);
        }
    }

    public boolean isFiltered(T value) {
        return stackPredicate != null && !stackPredicate.test(value);
    }
}
