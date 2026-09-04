package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import com.sighs.sophisticatedsorter.client.settings.ClientTrackedContainer;
import com.sighs.sophisticatedsorter.network.ClientboundContainerSettingsPayload;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

/**
 * {@link SettingsContainerMenu} over {@link ContainerSettingsWrapper}: the settings screen shell for
 * arbitrary containers.
 * <p>
 * The settings entry (top-right gear) opens this menu through a server-side menu swap. The server
 * pushes the {@link ContainerSettingsKey}, the real slot count and the container's current settings
 * contents to the client over the mod channel (see
 * {@code com.sighs.sophisticatedsorter.network.ClientboundOpenContainerSettingsPayload}) right
 * before opening the menu - Forge 1.20.1's open-screen packet carries no extra data, so the client
 * rebuilds its wrapper from that pushed state (staged through
 * {@link ClientContainerSettingsCache#stageOpenData}) instead of from a menu-open buffer. The
 * settings themselves are persisted server-side (a {@link ServerContainerSettingsStore} SavedData);
 * the client only ever sees copies that arrive in that payload or through
 * {@link ClientboundContainerSettingsPayload} pushes into {@link ClientContainerSettingsCache}.
 */
public class ContainerSettingsContainerMenu extends SettingsContainerMenu<ContainerSettingsWrapper> {
	private final ContainerSettingsStore store;
	private final ContainerSettingsKey key;
	/** Client-side copy of the last settings contents applied to this menu's handler. */
	private CompoundTag lastContentsNbt;

	public ContainerSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, ContainerSettingsWrapper storageWrapper,
			ContainerSettingsStore store, ContainerSettingsKey key) {
		super(menuType, windowId, player, storageWrapper);
		this.store = store;
		this.key = key;
	}

	/** Server-side factory for a real container target. */
	public static ContainerSettingsContainerMenu create(ContainerSettingsKey key, ContainerSettingsWrapper wrapper,
			ContainerSettingsStore store, int windowId, Player player) {
		return new ContainerSettingsContainerMenu(ModMenus.CONTAINER_SETTINGS.get(), windowId, player, wrapper, store, key);
	}

	/**
	 * Rebuilds the menu on the client from the state the server pushed before opening the menu (key,
	 * real slot count, settings contents). Mirrors {@code BackpackSettingsContainerMenu.fromBuffer}
	 * which reconstructs its context from the buffer - on Forge 1.20.1 that buffer is never filled,
	 * so the data comes from {@link ClientContainerSettingsCache}.
	 */
	public static ContainerSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
		ClientContainerSettingsCache.OpenData openData = ClientContainerSettingsCache.pollOpenData();
		ContainerSettingsKey key = openData != null ? openData.key() : ClientTrackedContainer.getCurrentKey();
		if (key == null) {
			key = ContainerSettingsKey.playerInventory();
		}
		int slots = openData != null ? openData.slots() : 0;
		CompoundTag contents = openData != null && openData.contents() != null
				? openData.contents()
				: ClientContainerSettingsCache.getOrCreateContents(key);
		ClientContainerSettingsCache.putContents(key, contents);
		ContainerSettingsStore store = ClientContainerSettingsCache.STORE;
		ContainerSettingsWrapper wrapper = key.isPlayerInventory()
				? ContainerSettingsWrapper.playerInventory(store, key,
						new ContainerInventoryHandles.PlayerInventoryHandle(playerInventory.player.getInventory()))
				: new ContainerSettingsWrapper(store, key, slots, null, Component.literal("container"));
		return new ContainerSettingsContainerMenu(ModMenus.CONTAINER_SETTINGS.get(), windowId, playerInventory.player, wrapper, store, key);
	}

	public ContainerSettingsKey getKey() {
		return key;
	}

	public ContainerSettingsStore getStore() {
		return store;
	}

	/**
	 * Server-side push of settings contents back to the client after an edit. The server is the
	 * authority (client category edits arrive via core's {@code SyncContainerClientDataPayload} and
	 * mutate the server store); the client mirrors those edits so the open settings screen reloads
	 * them (see {@link #detectSettingsChangeAndReload}). Mirrors
	 * {@code BackpackSettingsContainerMenu.broadcastChanges} which pushes changed settings contents.
	 */
	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		if (!player.level().isClientSide && store instanceof ServerContainerSettingsStore serverStore
				&& player instanceof ServerPlayer serverPlayer) {
			CompoundTag current = serverStore.getContents(key);
			if (lastContentsNbt == null || !lastContentsNbt.equals(current)) {
				lastContentsNbt = current == null ? null : current.copy();
				NetworkHandler.sendToClient(serverPlayer,
						new ClientboundContainerSettingsPayload(key, current));
			}
		}
	}

	@Override
	public void detectSettingsChangeAndReload() {
		if (player.level().isClientSide) {
			// Server-side category edits (sent by the client via core's SyncContainerClientDataPayload)
			// update the server store; the server pushes the changed contents back into the client
			// mirror (ClientboundContainerSettingsPayload). Core calls this method every frame from
			// SettingsScreen.render, so reload only when the mirror actually changed (mirrors
			// BackpackSettingsContainerMenu's lastSettingsNbt comparison).
			CompoundTag current = ClientContainerSettingsCache.getOrCreateContents(key);
			if (lastContentsNbt == null || !lastContentsNbt.equals(current)) {
				lastContentsNbt = current.copy();
				storageWrapper.getSettingsHandler().reloadFrom(current);
			}
		}
	}
}