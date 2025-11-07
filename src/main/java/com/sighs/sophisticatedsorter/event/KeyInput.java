package com.sighs.sophisticatedsorter.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.sophisticatedsorter.ModConfig;
import com.sighs.sophisticatedsorter.registry.ModKeybindings;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.ArrayList;
import java.util.List;

public class KeyInput {
    public static void register() {
        FabricInputEvents.MOUSE_BUTTON_POST.register(event -> {
            if (event.getAction() != InputConstants.PRESS) {
                return;
            }
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
                if (ModKeybindings.SORT_KEY.matchesMouse(event.getButton())) {
                    CoreUtils.serverSort(Minecraft.getInstance().player.containerMenu);
                }
            }
        });

        FabricInputEvents.KEY.register(event -> {
            if (event.getAction() != InputConstants.PRESS) {
                return;
            }
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof AbstractContainerScreen<?>) {
                if (ModKeybindings.SORT_KEY.matches(event.getKey(), event.getScanCode())) {
                    CoreUtils.serverSort(Minecraft.getInstance().player.containerMenu);
                }
                if (ModKeybindings.DISABLE_KEY.matches(event.getKey(), event.getScanCode())) {
                    try {
                        List<String> list = new ArrayList<>(ModConfig.INSTANCE.BLACK_LIST);
                        String current = ClientUtils.getScreenId(screen);
                        if (ClientUtils.isDisabledScreen(screen)) {
                            list.remove(current);
                        } else {
                            list.add(current);
                        }
                        list = list.stream().distinct().toList();
                        ModConfig.INSTANCE.BLACK_LIST = list;
                        AutoConfig.getConfigHolder(ModConfig.class).save();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }
}
