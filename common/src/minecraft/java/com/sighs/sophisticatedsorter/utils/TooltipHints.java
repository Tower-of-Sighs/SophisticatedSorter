package com.sighs.sophisticatedsorter.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Shared tooltip additions for Sophisticated Core controls. */
public final class TooltipHints {
    private TooltipHints() {
    }

    /** Translation key of the "drag with right button, press <key> to disable" hint. */
    public static final String DRAG_HINT_KEY = "gui.sophisticatedsorter.drag_hint";

    public static Component dragTooltipHint() {
        return Component.translatable(DRAG_HINT_KEY, ClientUtils.disableKeyDisplayName())
                .withStyle(ChatFormatting.GRAY);
    }

    public static List<Component> appendTooltipHint(List<Component> tooltip) {
        List<Component> result = new ArrayList<>(tooltip == null ? List.of() : tooltip);
        result.add(dragTooltipHint());
        return List.copyOf(result);
    }
}
