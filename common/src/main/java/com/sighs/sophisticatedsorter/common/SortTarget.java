package com.sighs.sophisticatedsorter.common;

/** The logical inventory domain to sort, independent of a loader's menu API. */
public enum SortTarget {
    INVENTORY,
    CONTAINER;

    public static SortTarget fromWireName(String value) {
        return "container".equalsIgnoreCase(value) ? CONTAINER : INVENTORY;
    }

    public String wireName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
