package com.sighs.sophisticatedsorter.common;

import java.util.Comparator;

/** Target-provided ordering primitives used by the shared comparator policy. */
public interface SortComparatorProvider<T> {
    static <T> SortComparatorProvider<T> of(
            final Comparator<T> byName,
            final Comparator<T> byMod,
            final Comparator<T> byCount,
            final Comparator<T> byTags,
            final Comparator<T> byPinyin) {
        return new SortComparatorProvider<T>() {
            @Override
            public Comparator<T> byName() {
                return byName;
            }

            @Override
            public Comparator<T> byMod() {
                return byMod;
            }

            @Override
            public Comparator<T> byCount() {
                return byCount;
            }

            @Override
            public Comparator<T> byTags() {
                return byTags;
            }

            @Override
            public Comparator<T> byPinyin() {
                return byPinyin;
            }
        };
    }

    Comparator<T> byName();

    Comparator<T> byMod();

    Comparator<T> byCount();

    Comparator<T> byTags();

    Comparator<T> byPinyin();
}
