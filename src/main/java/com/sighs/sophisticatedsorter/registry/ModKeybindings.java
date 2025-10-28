package com.sighs.sophisticatedsorter.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = SophisticatedSorter.MODID)
public class ModKeybindings {
    public static final KeyMapping CONTROL_KEY = new KeyMapping("key.sophisticatedsorter.sort",
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.SCANCODE,
            GLFW.GLFW_KEY_R,
            "key.categories.sophisticatedsorter"
    );

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(CONTROL_KEY);
    }
}
