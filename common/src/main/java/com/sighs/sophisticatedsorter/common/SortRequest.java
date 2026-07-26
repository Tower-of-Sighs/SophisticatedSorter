package com.sighs.sophisticatedsorter.common;

/** Validated, loader-neutral sort command. */
public final class SortRequest {
    private final SortCriterion criterion;
    private final SortTarget target;
    private final boolean pinyinOrder;

    public SortRequest(SortCriterion criterion, SortTarget target, boolean pinyinOrder) {
        this.criterion = criterion == null ? SortCriterion.NAME : criterion;
        this.target = target == null ? SortTarget.INVENTORY : target;
        this.pinyinOrder = pinyinOrder;
    }

    public static SortRequest fromWire(String criterion, String target, boolean pinyinOrder) {
        return new SortRequest(SortCriterion.fromWireName(criterion), SortTarget.fromWireName(target), pinyinOrder);
    }

    public SortCriterion criterion() { return criterion; }
    public SortTarget target() { return target; }
    public boolean pinyinOrder() { return pinyinOrder; }
}
