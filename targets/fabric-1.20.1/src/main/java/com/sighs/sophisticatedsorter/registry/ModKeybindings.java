package com.sighs.sophisticatedsorter.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeybindings {

    public static KeyMapping SORT_KEY;
    public static KeyMapping DISABLE_KEY;

    public static void registerKeyMapping() {
        SORT_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.sophisticatedsorter.sort",
                InputConstants.Type.SCANCODE,
                GLFW.GLFW_KEY_R,
                "key.categories.sophisticatedsorter"
        ));

        DISABLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.sophisticatedsorter.disable",
                InputConstants.Type.SCANCODE,
                GLFW.GLFW_KEY_U,
                "key.categories.sophisticatedsorter"
        ));
    }

    public static boolean matchesMouse(KeyMapping mapping, int button) {
        InputConstants.Key current = mapping.key;
        return current.getType() == InputConstants.Type.MOUSE && current.getValue() == button;
    }
}
