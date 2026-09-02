package com.sighs.sophisticatedsorter.utils;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.List;
import java.util.function.IntConsumer;

/** A Core button with the shared drag/disable tooltip appended. */
public class HintButton extends Button {
    public HintButton(Position position, ButtonDefinition definition, IntConsumer onClick) {
        super(position, definition, onClick);
    }

    @Override
    protected List<Component> getTooltip() {
        return TooltipHints.appendTooltipHint(super.getTooltip());
    }
}
