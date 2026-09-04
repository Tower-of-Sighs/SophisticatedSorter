package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Menu type registration for the container settings screens, mirroring how Sophisticated Backpacks
 * registers its settings menu (see {@code ModItems.SETTINGS_CONTAINER_TYPE}). The client-side menu
 * factory is Forge's {@link IForgeMenuType} so {@code ContainerSettingsContainerMenu.fromBuffer}
 * is invoked when the client constructs the menu.
 */
public final class ModMenus {
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SophisticatedSorter.MODID);

	public static final Supplier<MenuType<ContainerSettingsContainerMenu>> CONTAINER_SETTINGS = MENU_TYPES.register("container_settings",
			() -> IForgeMenuType.create(ContainerSettingsContainerMenu::fromBuffer));

	private ModMenus() {
	}

	public static void register(IEventBus modBus) {
		MENU_TYPES.register(modBus);
	}
}