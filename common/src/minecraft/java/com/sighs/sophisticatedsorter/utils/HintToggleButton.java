package com.sighs.sophisticatedsorter.utils;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/** A Core toggle button with the shared drag/disable tooltip appended. */
public class HintToggleButton<T extends Comparable<T>> extends ToggleButton<T> {
    public HintToggleButton(Position position, ButtonDefinition.Toggle<T> definition,
                            IntConsumer onClick, Supplier<T> getState) {
        super(position, definition, onClick, getState);
    }

    @Override
    protected List<Component> getTooltip(StateData stateData) {
        return TooltipHints.appendTooltipHint(super.getTooltip(stateData));
    }
}
