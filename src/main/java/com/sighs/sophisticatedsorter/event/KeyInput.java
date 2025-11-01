package com.sighs.sophisticatedsorter.event;

import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SophisticatedSorter.MODID, value = Dist.CLIENT)
public class KeyInput {
    @SubscribeEvent
    public static void sort(InputEvent.MouseButton event) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
            if (event.getButton() == ModKeybindings.CONTROL_KEY.getKey().getValue()) {
                CoreUtils.serverSort(Minecraft.getInstance().player.containerMenu);
            }
        }
    }
    @SubscribeEvent
    public static void sort(InputEvent.Key event) {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
            if (event.getKey() == ModKeybindings.CONTROL_KEY.getKey().getValue()) {
                CoreUtils.serverSort(Minecraft.getInstance().player.containerMenu);
            }
        }
    }
}
