package com.sighs.sophisticatedsorter.utils;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class KeyUtils {
    public static KeyMapping getKeyMapping(String desc) {
        for (KeyMapping keyMapping : Minecraft.getInstance().options.keyMappings) {
            if (desc.equals(keyMapping.getName())) {
                return keyMapping;
            }
        }
        return null;
    }
}
