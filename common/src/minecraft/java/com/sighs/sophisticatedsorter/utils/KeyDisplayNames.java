package com.sighs.sophisticatedsorter.utils;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Locale;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Resolves the display name for a key binding, including scancode bindings. */
public final class KeyDisplayNames {
    private KeyDisplayNames() {
    }

    public static String displayName(KeyMapping mapping) {
        InputConstants.Key key = mapping.key;
        if (key.getType() == InputConstants.Type.SCANCODE) {
            String name = GLFW.glfwGetKeyName(-1, key.getValue());
            if (name == null) {
                name = GLFW.glfwGetKeyName(key.getValue(), 0);
            }
            if (name != null) {
                return name.toUpperCase(Locale.ROOT);
            }
        }
        return mapping.getTranslatedKeyMessage().getString();
    }
}

