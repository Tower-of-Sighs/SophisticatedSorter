package com.sighs.sophisticatedsorter.common;

/** Pure layout arithmetic shared by all AbstractContainerScreen bridges. */
public final class ContainerScreenLayout {
    private ContainerScreenLayout() {
    }

    public static Positions positions(
            int left,
            int top,
            int imageWidth,
            int inventoryLabelX,
            int inventoryLabelY,
            boolean inventoryScreen,
            int inventoryRight,
            int inventoryTop) {
        int topToggleX = left + imageWidth - 19;
        int topSortX = left + imageWidth - 31;
        int bottomToggleX = left + inventoryLabelX + 149;
        int bottomSortX = left + inventoryLabelX + 137;
        if (!inventoryScreen) {
            bottomToggleX = left + inventoryRight + 5;
            bottomSortX = left + inventoryRight - 7;
            int y = top + inventoryTop - 13;
            return new Positions(topToggleX, top + 4, topSortX, top + 4,
                    bottomToggleX, y, bottomSortX, y);
        }
        return new Positions(topToggleX, top + 4, topSortX, top + 4,
                bottomToggleX, top + inventoryLabelY - 2,
                bottomSortX, top + inventoryLabelY - 2);
    }

    public static int maxInventoryX(int current, int slotX) {
        return current == 0 ? slotX : Math.max(current, slotX);
    }

    public static int minInventoryY(int current, int slotY) {
        return current == 0 ? slotY : Math.min(current, slotY);
    }

    public static final class Positions {
        private final int topToggleX;
        private final int topToggleY;
        private final int topSortX;
        private final int topSortY;
        private final int bottomToggleX;
        private final int bottomToggleY;
        private final int bottomSortX;
        private final int bottomSortY;

        private Positions(int topToggleX, int topToggleY, int topSortX, int topSortY,
                          int bottomToggleX, int bottomToggleY, int bottomSortX, int bottomSortY) {
            this.topToggleX = topToggleX;
            this.topToggleY = topToggleY;
            this.topSortX = topSortX;
            this.topSortY = topSortY;
            this.bottomToggleX = bottomToggleX;
            this.bottomToggleY = bottomToggleY;
            this.bottomSortX = bottomSortX;
            this.bottomSortY = bottomSortY;
        }

        public int topToggleX() { return topToggleX; }
        public int topToggleY() { return topToggleY; }
        public int topSortX() { return topSortX; }
        public int topSortY() { return topSortY; }
        public int bottomToggleX() { return bottomToggleX; }
        public int bottomToggleY() { return bottomToggleY; }
        public int bottomSortX() { return bottomSortX; }
        public int bottomSortY() { return bottomSortY; }
    }
}
