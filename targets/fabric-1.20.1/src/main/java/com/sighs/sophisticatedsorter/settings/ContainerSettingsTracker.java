package com.sighs.sophisticatedsorter.settings;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.sighs.sophisticatedsorter.network.ClientboundContainerSettingsPayload;
import com.sighs.sophisticatedsorter.network.ClientboundTrackedContainerKeyPayload;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side record of which container each player currently has open (or just had open before
 * switching to the settings screen).
 * <p>
 * The settings entry is a small gear on vanilla container screens and it only knows "the player has
 * a container open". It therefore sends an anonymous open request and the server resolves the target
 * from this tracker instead of guessing a dimension/position on the client. The tracker is
 * maintained purely through server-side hooks - the Fabric 1.20.1 counterpart of the NeoForge
 * container open/close events, which this loader does not expose:
 * <ul>
 * <li>{@link #onPlayerOpenMenu} is called from a {@code ServerPlayer.openMenu} RETURN inject (the
 * fabric equivalent of {@code PlayerContainerEvent.Open}); it records the key of the container menu
 * the player just opened. The key is derived server-side by matching the menu's storage container
 * against the block entities around the player's position (see
 * {@link ContainerSettingsKeyResolver}). A menu whose container does not resolve to a supported
 * block entity is not tracked.</li>
 * <li>{@link #onPlayerCloseContainer} is called from a {@code ServerPlayer.doCloseContainer} HEAD
 * inject (the fabric equivalent of {@code PlayerContainerEvent.Close}); it clears the record so a
 * subsequent gear click cannot act on a stale container. {@code doCloseContainer} is the universal
 * close funnel on this version: the client close packet, the vanilla {@code closeContainer} and the
 * Fabric screen-handler soft close during menu swaps all pass through it.</li>
 * <li>{@link #onPlayerChangedDimension} (dimension-change inject) and the Fabric API
 * {@link ServerPlayConnectionEvents#DISCONNECT} also clear it as belt and braces.</li>
 * </ul>
 */
public final class ContainerSettingsTracker {
	private static final ContainerSettingsTracker INSTANCE = new ContainerSettingsTracker();
	private final Map<UUID, ContainerSettingsKey> openKeyByPlayer = new ConcurrentHashMap<>();
	/**
	 * The container the player was viewing before they opened the settings screen, per player.
	 * Unlike {@link #openKeyByPlayer} this survives the menu swap (opening the settings menu closes
	 * the old container, which clears the open key), so the return flow can reopen the original
	 * container. Cleared once the settings screen is closed.
	 */
	private final Map<UUID, ContainerSettingsKey> returnKeyByPlayer = new ConcurrentHashMap<>();

	private ContainerSettingsTracker() {
	}

	/** Registers the server-lifecycle hooks (called from the mod constructor). */
	public static void register() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayer player = handler.player;
			if (player != null) {
				server.execute(() -> get().untrack(player));
			}
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
	 * Container-open tracking (see the class comment). Also pushes the opened key and the
	 * authoritative settings contents to the client so vanilla screens can render slot highlights.
	 */
	public static void onPlayerOpenMenu(ServerPlayer serverPlayer) {
		if (serverPlayer.containerMenu == null) {
			return;
		}
		ContainerSettingsKey key = ContainerSettingsKeyResolver.resolveKey(serverPlayer, serverPlayer.containerMenu);
		if (key != null) {
			get().track(serverPlayer, key);
		}
		// Tell the client which container it just opened so vanilla screens can render slot highlights
		// (the client cannot resolve the key itself - its menu slots wrap a SimpleContainer), and push
		// the authoritative settings contents so the highlights/memory ghosts have data.
		NetworkHandler.sendToClient(serverPlayer, NetworkHandler.TRACKED_CONTAINER_KEY_ID,
				new ClientboundTrackedContainerKeyPayload(key));
		if (key != null && !key.isPlayerInventory()) {
			ServerContainerSettingsStore store = ServerContainerSettingsStore.get();
			if (store != null) {
				NetworkHandler.sendToClient(serverPlayer, NetworkHandler.CONTAINER_SETTINGS_ID,
						new ClientboundContainerSettingsPayload(key, store.getContents(key)));
			}
		}
	}

	/**
	 * Container-close tracking (see the class comment). Also clears any stale armed state of
	 * {@link ContainerMemorySlotGuard} so a leftover guard can never gate a later menu.
	 */
	public static void onPlayerCloseContainer(ServerPlayer serverPlayer) {
		get().untrack(serverPlayer);
		NetworkHandler.sendToClient(serverPlayer, NetworkHandler.TRACKED_CONTAINER_KEY_ID,
				new ClientboundTrackedContainerKeyPayload(null));
	}

	/** Dimension-change belt-and-braces cleanup. */
	public static void onPlayerChangedDimension(ServerPlayer serverPlayer) {
		get().untrack(serverPlayer);
	}
}