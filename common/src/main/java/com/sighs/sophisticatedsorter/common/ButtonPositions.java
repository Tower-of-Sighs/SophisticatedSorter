package com.sighs.sophisticatedsorter.common;

/** Immutable per-screen offsets for the sorter sort and transfer button groups. */
public final class ButtonPositions {
    public static final ButtonPositions ZERO = new ButtonPositions(0, 0, 0, 0);

    private final int sortX;
    private final int sortY;
    private final int transferX;
    private final int transferY;

    public ButtonPositions(int sortX, int sortY, int transferX, int transferY) {
        this.sortX = sortX;
        this.sortY = sortY;
        this.transferX = transferX;
        this.transferY = transferY;
    }

    public int sortX() {
        return sortX;
    }

    public int sortY() {
        return sortY;
    }

    public int transferX() {
        return transferX;
    }

    public int transferY() {
        return transferY;
    }
}
