package com.sighs.sophisticatedsorter.common;

import java.util.Comparator;

/** Shared policy for selecting and applying the configured item ordering. */
public final class SortComparatorSelection {
    private SortComparatorSelection() {
    }

    public static <T> Comparator<T> select(
            SortCriterion criterion,
            boolean pinyinOrder,
            SortComparatorProvider<T> provider) {
        SortCriterion effectiveCriterion = criterion == null ? SortCriterion.NAME : criterion;
        Comparator<T> comparator;
        switch (effectiveCriterion) {
            case COUNT:
                comparator = provider.byCount();
                break;
            case TAGS:
                comparator = provider.byTags();
                break;
            case MOD:
                comparator = provider.byMod();
                break;
            case NAME:
            default:
                comparator = pinyinOrder ? provider.byPinyin() : provider.byName();
                break;
        }
        return pinyinOrder ? comparator.thenComparing(provider.byPinyin()) : comparator;
    }
}
