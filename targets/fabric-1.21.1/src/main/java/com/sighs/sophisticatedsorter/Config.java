package com.sighs.sophisticatedsorter;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeConfig;

/**
 * Server-side configuration for the container settings feature.
 * <p>
 * The client-side settings (sort criterion, filters, button positions, ...) live in the existing
 * AutoConfig-based {@link ModConfig}; this class only hosts the Sophisticated Core
 * {@link StackUpgradeConfig}. Core's {@code InventoryHandler} asks its stack-upgrade config for
 * item stack limits whenever the handler has real slots, and {@code StackUpgradeConfig
 * .canStackItem} only returns safely when the value it reads belongs to a registered (and
 * therefore loaded) spec - mirroring how Sophisticated Backpacks hosts its {@code StackUpgradeConfig}
 * in its server config. A bare {@code new StackUpgradeConfig(new ModConfigSpec.Builder())} (as
 * core's {@code NoopStorageWrapper} uses) only survives a zero-slot handler that never asks for a
 * stack limit.
 * <p>
 * The spec is registered server-side through Forge Config API Port's NeoForge registry (the same
 * mechanism the fabric build of Sophisticated Core uses to register its own configs); see
 * {@link #register()}.
 */
public final class Config {
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        var serverSpec = new ModConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverSpec.getRight();
        SERVER = serverSpec.getLeft();
    }

    private Config() {
    }

    /** Registers the server config spec so core's StackUpgradeConfig can read from a loaded spec. */
    public static void register() {
        fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE
                .register(SophisticatedSorter.MODID, net.neoforged.fml.config.ModConfig.Type.SERVER, SERVER_SPEC);
    }

    public static final class Server {
        public final StackUpgradeConfig stackUpgrade;

        public Server(ModConfigSpec.Builder builder) {
            this.stackUpgrade = new StackUpgradeConfig(builder);
        }
    }
}