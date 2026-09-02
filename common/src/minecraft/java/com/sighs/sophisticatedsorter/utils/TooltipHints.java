package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.common.TooltipText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Shared tooltip additions for Sophisticated Core controls. */
public final class TooltipHints {
    private TooltipHints() {
    }

    public static Component dragTooltipHint() {
        return Component.literal(TooltipText.dragTooltip(ClientUtils.disableKeyDisplayName()))
                .withStyle(ChatFormatting.GRAY);
    }

    public static List<Component> appendTooltipHint(List<Component> tooltip) {
        List<Component> result = new ArrayList<>(tooltip == null ? List.of() : tooltip);
        result.add(dragTooltipHint());
        return List.copyOf(result);
    }
}
