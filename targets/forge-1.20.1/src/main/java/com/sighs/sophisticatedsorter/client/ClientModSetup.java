package com.sighs.sophisticatedsorter.client;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.client.settings.ContainerSettingsScreen;
import com.sighs.sophisticatedsorter.settings.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only registration for stage 2: binds {@link ContainerSettingsScreen} to the container
 * settings menu type through the vanilla {@link MenuScreens} registry. Forge 1.20.1 has no
 * menu-screen registration event; the binding happens in client setup (mirroring how Sophisticated
 * Backpacks registers its settings screen in {@code ModItemsClient}). The class only loads on the
 * client distribution (see the {@code Dist.CLIENT} subscriber restriction).
 */
@Mod.EventBusSubscriber(modid = SophisticatedSorter.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModSetup {
	private ClientModSetup() {
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> MenuScreens.register(ModMenus.CONTAINER_SETTINGS.get(), ContainerSettingsScreen::constructScreen));
	}
}