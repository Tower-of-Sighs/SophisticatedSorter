package com.sighs.sophisticatedsorter.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;

import java.util.List;
import java.util.function.Consumer;

/** The Core transfer control, kept public for ordinary vanilla screens. */
final class ShiftTransferButton extends Button {
    private final ButtonDefinition definition;
    private final ButtonDefinition shiftDefinition;

    ShiftTransferButton(Position position, Consumer<Boolean> transfer,
                        ButtonDefinition definition, ButtonDefinition shiftDefinition) {
        super(position, definition, button -> {
            if (button == 0) {
                transfer.accept(!Minecraft.getInstance().hasShiftDown());
            }
        });
        this.definition = definition;
        this.shiftDefinition = shiftDefinition;
    }

    @Override
    protected void extractWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ButtonDefinition active = Minecraft.getInstance().hasShiftDown() ? shiftDefinition : definition;
        if (active.getForegroundTexture() != null) {
            GuiHelper.blit(graphics, getX(), getY(), active.getForegroundTexture());
        }
    }

    @Override
    protected List<Component> getTooltip() {
        return ScreenInit.appendTooltipHint(
                (Minecraft.getInstance().hasShiftDown() ? shiftDefinition : definition).getTooltip());
    }
}
