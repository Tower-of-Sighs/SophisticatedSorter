package com.sighs.sophisticatedsorter.common;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

/** Shared locale-aware name ordering; targets provide their own item-name extractor. */
public final class PinyinOrdering {
    private PinyinOrdering() {
    }

    public static <T> Comparator<T> byName(Function<T, String> nameExtractor) {
        final Collator collator = Collator.getInstance(Locale.CHINA);
        return new Comparator<T>() {
            @Override
            public int compare(T left, T right) {
                return collator.compare(nameExtractor.apply(left), nameExtractor.apply(right));
            }
        };
    }
}
