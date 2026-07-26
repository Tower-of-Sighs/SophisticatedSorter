package com.sighs.sophisticatedsorter.event;


import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.sophisticatedsorter.Config;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SophisticatedSorter.MODID, value = Dist.CLIENT)
public class KeyInput {
    @SubscribeEvent
    public static void sort(InputEvent.MouseButton.Post event) {
        if (event.getAction() != InputConstants.PRESS) return;
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            if (event.getButton() == ModKeybindings.SORT_KEY.getKey().getValue()) {
                ClientUtils.serverSort();
            }
        }
    }
    @SubscribeEvent
    public static void sort(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) return;
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            if (event.getKey() == ModKeybindings.SORT_KEY.getKey().getValue()) {
                ClientUtils.serverSort();
            }
        }
    }

    @SubscribeEvent
    public static void disable(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) return;
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen<?>) {
            if (event.getKey() == ModKeybindings.DISABLE_KEY.getKey().getValue()) {
                try {
                    List<String> list = new ArrayList<>(Config.BLACKLIST.get());
                    String current = ClientUtils.getScreenId(screen);
                    if (ClientUtils.isDisabledScreen(screen)) {
                        list.remove(current);
                    } else list.add(current);
                    Config.BLACKLIST.set(list);
                    Config.BLACKLIST.save();
                } catch (Exception ignored) {}
            }
        }
    }
}
