package com.sighs.sophisticatedsorter;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;

/**
 * Server-side config that hosts the Sophisticated Core {@link StackUpgradeConfig}. Core's
 * {@code InventoryHandler} asks its stack-upgrade config for item stack limits whenever the
 * handler has real slots, and {@code StackUpgradeConfig.canStackItem} only returns safely when
 * the value it reads belongs to a registered (and therefore loaded) spec - mirroring how
 * Sophisticated Backpacks hosts its {@code StackUpgradeConfig} in its server config.
 * <p>
 * Platform adaptation: the 1.21.1 reference registers the spec through NeoForge's
 * {@code ModConfigSpec}/{@code registerConfig}; on Fabric 1.20.1 the loader-neutral Forge Config
 * API Port provides both the spec type ({@link ForgeConfigSpec}) and the registration entry point
 * ({@link ForgeConfigRegistry}) that this core's fabric port already depends on.
 */
public final class Config {
	public static final Server SERVER;
	public static final ForgeConfigSpec SERVER_SPEC;

	private Config() {
	}

	static {
		var configPair = new ForgeConfigSpec.Builder().configure(Server::new);
		SERVER_SPEC = configPair.getRight();
		SERVER = configPair.getLeft();
	}

	public static void register() {
		ForgeConfigRegistry.INSTANCE.register(SophisticatedSorter.MODID, ModConfig.Type.SERVER, SERVER_SPEC);
	}

	public static final class Server {
		public final StackUpgradeConfig stackUpgrade;

		public Server(ForgeConfigSpec.Builder builder) {
			this.stackUpgrade = new StackUpgradeConfig(builder);
		}
	}
}