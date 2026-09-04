package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

/**
 * Menu type registration for the container settings screens, mirroring how Sophisticated Backpacks
 * registers its settings menu (see {@code ModItems.SETTINGS_CONTAINER_TYPE}).
 * <p>
 * Platform adaptation: the 1.21.1 reference registers the menu through NeoForge's
 * {@code IMenuTypeExtension.create} (a buffered menu factory). On Fabric the same buffered opening
 * data is delivered through the Fabric API {@link ExtendedScreenHandlerType}, whose extended
 * factory receives the {@link net.minecraft.network.FriendlyByteBuf} the server-side open flow
 * wrote into {@link ContainerSettingsContainerMenu#fromBuffer}.
 */
public final class ModMenus {
	public static final MenuType<ContainerSettingsContainerMenu> CONTAINER_SETTINGS =
			new ExtendedScreenHandlerType<>(ContainerSettingsContainerMenu::fromBuffer);

	private ModMenus() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.MENU,
				new ResourceLocation(SophisticatedSorter.MODID, "container_settings"), CONTAINER_SETTINGS);
	}
}