package com.sighs.sophisticatedsorter.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

public class VisualStorageScreen extends StorageScreenBase {
    public VisualStorageScreen() {
        super(new VisualStorageContainerMenu(Minecraft.getInstance().player), Minecraft.getInstance().player.getInventory(), Component.empty());
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected String getStorageSettingsTabTooltip() {
        return "";
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // This screen is only a transient visual adapter and has no independent background.
    }
}
