package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.network.ClientboundContainerSettingsPayload;
import com.sighs.sophisticatedsorter.network.ClientboundTrackedContainerKeyPayload;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side record of which container each player currently has open (or just had open before
 * switching to the settings screen).
 * <p>
 * The settings entry is a small gear on vanilla container screens and it only knows "the player has
 * a container open". It therefore sends an anonymous open request and the server resolves the target
 * from this tracker instead of guessing a dimension/position on the client. The tracker is
 * maintained purely through events:
 * <ul>
 * <li>{@link PlayerContainerEvent.Open} records the key of the container menu the player just
 * opened. The key is derived server-side by matching the menu's storage container against the block
 * entities around the player's position (see {@link ContainerSettingsKeyResolver}). A menu whose
 * container does not resolve to a supported block entity is not tracked.</li>
 * <li>{@link PlayerContainerEvent.Close} clears the record so a subsequent gear click cannot act on
 * a stale container.</li>
 * <li>{@link PlayerEvent.PlayerLoggedOutEvent} and {@link PlayerEvent.PlayerChangedDimensionEvent}
 * also clear it as belt and braces.</li>
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

	/** Subscribes the static event handlers to the game event bus (called from the mod constructor). */
	public static void register(IEventBus gameBus) {
		gameBus.register(ContainerSettingsTracker.class);
	}

	public static ContainerSettingsTracker get() {
		return INSTANCE;
	}

	@Nullable
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
	@Nullable
	public ContainerSettingsKey getAndClearReturnKey(ServerPlayer player) {
		UUID uuid = player.getUUID();
		ContainerSettingsKey key = returnKeyByPlayer.get(uuid);
		if (key != null) {
			returnKeyByPlayer.remove(uuid);
		}
		return key;
	}

	@SubscribeEvent
	public static void onPlayerContainerOpen(PlayerContainerEvent.Open event) {
		if (!(event.getEntity() instanceof ServerPlayer serverPlayer)
				|| !(event.getContainer() instanceof AbstractContainerMenu menu)) {
			return;
		}
		ContainerSettingsKey key = ContainerSettingsKeyResolver.resolveKey(serverPlayer, menu);
		if (key != null) {
			get().track(serverPlayer, key);
		}
		// Tell the client which container it just opened so vanilla screens can render slot highlights
		// (the client cannot resolve the key itself - its menu slots wrap a SimpleContainer), and push
		// the authoritative settings contents so the highlights/memory ghosts have data.
		PacketDistributor.sendToPlayer(serverPlayer, new ClientboundTrackedContainerKeyPayload(key));
		if (key != null && !key.isPlayerInventory()) {
			ServerContainerSettingsStore store = ServerContainerSettingsStore.get();
			if (store != null) {
				PacketDistributor.sendToPlayer(serverPlayer,
						new ClientboundContainerSettingsPayload(key, store.getContents(key)));
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerContainerClose(PlayerContainerEvent.Close event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			get().untrack(serverPlayer);
			PacketDistributor.sendToPlayer(serverPlayer, new ClientboundTrackedContainerKeyPayload(null));
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			get().untrack(serverPlayer);
		}
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			get().untrack(serverPlayer);
		}
	}
}