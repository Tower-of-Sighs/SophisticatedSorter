package com.sighs.sophisticatedsorter.common;

/** Plain-text UI snippets shared by every Minecraft target. */
public final class TooltipText {
    private TooltipText() {
    }

    public static String dragTooltip(String keyName) {
        return new String(new char[]{'右','键','拖','动','，'})
                + keyName
                + new String(new char[]{'键','关','闭'});
    }
}
