package com.sighs.sophisticatedsorter.settings;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.sighs.sophisticatedsorter.network.ClientboundContainerSettingsPayload;
import com.sighs.sophisticatedsorter.network.ClientboundTrackedContainerKeyPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Server-side record of which container each player currently has open (or just had open before
 * switching to the settings screen).
 * <p>
 * The settings entry is a small gear on vanilla container screens and it only knows "the player has
 * a container open". It therefore sends an anonymous open request and the server resolves the target
 * from this tracker instead of guessing a dimension/position on the client.
 * <p>
 * NeoForge maintains this through the {@code PlayerContainerEvent} open/close events; Fabric has no
 * such events, so the tracker is fed by two target-local mixins instead:
 * <ul>
 * <li>{@code ServerPlayerOpenMenuMixin} calls {@link #onContainerOpened} after
 * {@code ServerPlayer.openMenu} succeeds. The key is derived server-side by matching the menu's
 * storage container against the block entities around the player's position (see
 * {@link ContainerSettingsKeyResolver}). A menu whose container does not resolve to a supported
 * block entity is not tracked.</li>
 * <li>{@code ContainerMenuRemovedMixin} calls {@link #onContainerClosed} from
 * {@code AbstractContainerMenu.removed} (server-side only) so a subsequent gear click cannot act on
 * a stale container.</li>
 * <li>{@link #registerDisconnectHandlers()} clears the record on logout and on dimension change
 * (entity unload) as belt and braces.</li>
 * </ul>
 */
public final class ContainerSettingsTracker {
	private static final ContainerSettingsTracker INSTANCE = new ContainerSettingsTracker();
	private final Map<UUID, ContainerSettingsKey> openKeyByPlayer = new ConcurrentHashMap<>();
	/**
	 * The container the player was viewing before they opened the settings screen, per player.
	 * Unlike {@link #openKeyByPlayer} this survives the menu swap (opening the settings menu fires a
	 * container-close event that clears the open key), so the return flow can reopen the original
	 * container. Cleared once the settings screen is closed.
	 */
	private final Map<UUID, ContainerSettingsKey> returnKeyByPlayer = new ConcurrentHashMap<>();

	private ContainerSettingsTracker() {
	}

	/** Clears tracking on logout and dimension change (the player entity unloads from its level). */
	public static void registerDisconnectHandlers() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (entity instanceof ServerPlayer serverPlayer) {
				INSTANCE.untrack(serverPlayer);
			}
		});
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			INSTANCE.untrack(handler.player);
		});
	}

	public static ContainerSettingsTracker get() {
		return INSTANCE;
	}

		public ContainerSettingsKey getOpenKey(ServerPlayer player) {
		return openKeyByPlayer.get(player.getUUID());
	}

	/** Direct put used by the open-settings flow when the gear click arrives without a prior open event. */
	public void track(ServerPlayer player, ContainerSettingsKey key) {
		if (key != null) {
			openKeyByPlayer.put(player.getUUID(), key);
		}
	}

	public void untrack(ServerPlayer player) {
		openKeyByPlayer.remove(player.getUUID());
	}

	/** Records the container the settings screen should return to when it closes. */
	public void setReturnKey(ServerPlayer player, ContainerSettingsKey key) {
		if (key != null) {
			returnKeyByPlayer.put(player.getUUID(), key);
		}
	}

	/** Returns and clears the return target for the player (called when the settings screen closes). */
		public ContainerSettingsKey getAndClearReturnKey(ServerPlayer player) {
		UUID uuid = player.getUUID();
		ContainerSettingsKey key = returnKeyByPlayer.get(uuid);
		if (key != null) {
			returnKeyByPlayer.remove(uuid);
		}
		return key;
	}

	/**
	 * Records the container of a menu the player just opened (server-side, from the
	 * {@code ServerPlayer.openMenu} RETURN injection) and pushes the tracked key plus the
	 * authoritative settings contents to the client.
	 */
	public void onContainerOpened(ServerPlayer serverPlayer) {
		AbstractContainerMenu menu = serverPlayer.containerMenu;
		ContainerSettingsKey key = ContainerSettingsKeyResolver.resolveKey(serverPlayer, menu);
		if (key != null) {
			track(serverPlayer, key);
		}
		// Tell the client which container it just opened so vanilla screens can render slot highlights
		// (the client cannot resolve the key itself - its menu slots wrap a SimpleContainer), and push
		// the authoritative settings contents so the highlights/memory ghosts have data.
		ServerPlayNetworking.send(serverPlayer, new ClientboundTrackedContainerKeyPayload(key));
		if (key != null && !key.isPlayerInventory()) {
			ServerContainerSettingsStore store = ServerContainerSettingsStore.get();
			if (store != null) {
				ServerPlayNetworking.send(serverPlayer,
						new ClientboundContainerSettingsPayload(key, store.getContents(key)));
			}
		}
	}

	/** Clears the record when the player's menu is removed server-side (ESC, death, ...). */
	public void onContainerClosed(ServerPlayer serverPlayer) {
		untrack(serverPlayer);
		ServerPlayNetworking.send(serverPlayer, new ClientboundTrackedContainerKeyPayload(null));
	}
}