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
        InputEvent.MOUSE_BUTTON_POST.register(KeyInput::sort);
        InputEvent.KEY.register(KeyInput::keyPress);
    }

    private static void sort(int button, int action, int modifiers) {
        if (action != InputConstants.PRESS) {
            return;
        }
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
            if (ModKeybindings.matchesMouse(ModKeybindings.SORT_KEY, button)) {
                ClientUtils.serverSort();
            }
        }
    }

    private static void keyPress(int key, int scancode, int action, int modifiers) {
        if (action != InputConstants.PRESS) {
            return;
        }
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen<?>) {
            if (ModKeybindings.SORT_KEY.matches(key, scancode)) {
                ClientUtils.serverSort();
            }
            if (ModKeybindings.DISABLE_KEY.matches(key, scancode)) {
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
    }
}
