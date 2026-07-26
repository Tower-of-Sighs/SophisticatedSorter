package com.sighs.sophisticatedsorter.common;

import java.util.Locale;

/** Loader-independent representation of the sort choices exposed to clients. */
public enum SortCriterion {
    NAME,
    MOD,
    COUNT,
    TAGS;

    public static SortCriterion fromWireName(String value) {
        if (value == null) {
            return NAME;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NAME;
        }
    }

    public String wireName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
