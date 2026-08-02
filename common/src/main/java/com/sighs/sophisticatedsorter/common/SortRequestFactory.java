package com.sighs.sophisticatedsorter.common;

/** Builds the command sent by a client after its target screen has been validated. */
public final class SortRequestFactory {
    private SortRequestFactory() {
    }

    public static SortRequest forScreen(
            boolean validContainer,
            SortCriterion criterion,
            boolean pinyinOrder) {
        return new SortRequest(
                criterion,
                validContainer ? SortTarget.CONTAINER : SortTarget.INVENTORY,
                pinyinOrder);
    }
}
