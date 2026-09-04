package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.util.IMenuTypeExtension;

/**
 * Menu type registration for the container settings screens, mirroring how Sophisticated Backpacks
 * registers its settings menu (see {@code ModItems.SETTINGS_CONTAINER_TYPE}). The fabric port of
 * core exposes {@link IMenuTypeExtension#create} (built on Fabric API's
 * {@code ExtendedScreenHandlerType}) which gives the client menu access to the opening buffer the
 * server writes - the fabric equivalent of NeoForge's {@code IMenuTypeExtension.create}.
 */
public final class ModMenus {
	public static final MenuType<ContainerSettingsContainerMenu> CONTAINER_SETTINGS;

	static {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SophisticatedSorter.MODID, "container_settings");
		// Registry.register permits overwriting a previous value until the registry is frozen; this
		// guard makes the (dev-only) double initialization harmless.
		CONTAINER_SETTINGS = BuiltInRegistries.MENU.get(id) instanceof MenuType<?> existing
				? (MenuType<ContainerSettingsContainerMenu>) existing
				: Registry.register(BuiltInRegistries.MENU, id, IMenuTypeExtension.create(ContainerSettingsContainerMenu::fromBuffer));
	}

	private ModMenus() {
	}

	/** Forces the menu type registration to run (called from the mod initializer). */
	public static void register() {
		if (CONTAINER_SETTINGS == null) {
			throw new IllegalStateException("container settings menu type was not registered");
		}
	}
}