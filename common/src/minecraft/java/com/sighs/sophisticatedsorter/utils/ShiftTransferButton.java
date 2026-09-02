package com.sighs.sophisticatedsorter.utils;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.List;
import java.util.function.Consumer;

/** The Core shift-aware transfer control, kept public for ordinary container screens. */
public class ShiftTransferButton extends Button {
    private final ButtonDefinition definition;
    private final ButtonDefinition shiftDefinition;

    public ShiftTransferButton(Position position, Consumer<Boolean> transfer,
                               ButtonDefinition definition, ButtonDefinition shiftDefinition) {
        super(position, definition, button -> {
            if (button == 0) {
                transfer.accept(!Screen.hasShiftDown());
            }
        });
        this.definition = definition;
        this.shiftDefinition = shiftDefinition;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ButtonDefinition active = Screen.hasShiftDown() ? shiftDefinition : definition;
        if (active.getForegroundTexture() != null) {
            GuiHelper.blit(guiGraphics, getX(), getY(), active.getForegroundTexture());
        }
    }

    @Override
    protected List<Component> getTooltip() {
        return TooltipHints.appendTooltipHint(
                (Screen.hasShiftDown() ? shiftDefinition : definition).getTooltip());
    }
}
