package com.sighs.sophisticatedsorter.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.PlatformClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, modid = SophisticatedSorter.MODID)
public class ModKeybindings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(SophisticatedSorter.MODID, "keybinds"));
    static {
        ClientUtils.installPlatform(PlatformClient.INSTANCE);
    }
    public static final KeyMapping SORT_KEY = new KeyMapping("key.sophisticatedsorter.sort",
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    public static final KeyMapping DISABLE_KEY = new KeyMapping("key.sophisticatedsorter.disable",
            KeyConflictContext.GUI,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SORT_KEY);
        event.register(DISABLE_KEY);
    }
}
