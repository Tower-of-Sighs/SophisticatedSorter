package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu type registration for the container settings screens, mirroring how Sophisticated Backpacks
 * registers its settings menu (see {@code ModItems.SETTINGS_CONTAINER_TYPE}).
 */
public final class ModMenus {
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, SophisticatedSorter.MODID);

	public static final Supplier<MenuType<ContainerSettingsContainerMenu>> CONTAINER_SETTINGS = MENU_TYPES.register("container_settings",
			() -> IMenuTypeExtension.create(ContainerSettingsContainerMenu::fromBuffer));

	private ModMenus() {
	}

	public static void register(IEventBus modBus) {
		MENU_TYPES.register(modBus);
	}
}
