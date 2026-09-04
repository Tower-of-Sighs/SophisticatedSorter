package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

/**
 * {@link SettingsContainerMenu} over {@link ContainerSettingsWrapper}: the settings screen shell for
 * arbitrary containers.
 * <p>
 * The settings entry (top-right gear) opens this menu through a server-side menu swap. The server
 * opens it via a {@code SophisticatedMenuProvider} whose opening buffer carries the
 * {@link ContainerSettingsKey}, the real slot count and the container's current settings contents -
 * the exact data {@link #fromBuffer} needs to rebuild the wrapper and screen on the client. The
 * settings themselves are persisted server-side (a {@link ServerContainerSettingsStore} SavedData);
 * the client only ever sees copies that arrive in the opening buffer or through
 * {@code com.sighs.sophisticatedsorter.network.ClientboundContainerSettingsPayload} pushes into
 * {@link ClientContainerSettingsCache}.
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
	 * Rebuilds the menu on the client from the opening payload written by the server-side open
	 * helper (key, then real slot count, then settings contents). Mirrors
	 * {@code BackpackSettingsContainerMenu.fromBuffer} which reconstructs its context from the buffer.
	 */
	public static ContainerSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
		ContainerSettingsKey key = ContainerSettingsKey.fromBuffer(buf);
		int slots = buf.readVarInt();
		CompoundTag contents = buf.readNbt();
		ContainerSettingsStore store = ClientContainerSettingsCache.STORE;
		if (contents != null) {
			ClientContainerSettingsCache.putContents(key, contents);
		}
		ContainerSettingsWrapper wrapper = key.isPlayerInventory()
				? ContainerSettingsWrapper.playerInventory(store, key,
						new ContainerInventoryHandles.PlayerInventoryHandle(playerInventory.player.getInventory()),
						playerInventory.player.level().registryAccess())
				: new ContainerSettingsWrapper(store, key, slots, null, Component.literal("container"),
						playerInventory.player.level().registryAccess());
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
	 * them (see {@link #detectSettingsChangeAndReload}).
	 */
	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		if (!player.level().isClientSide() && store instanceof ServerContainerSettingsStore serverStore
				&& player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			CompoundTag current = serverStore.getContents(key);
			if (lastContentsNbt == null || !lastContentsNbt.equals(current)) {
				lastContentsNbt = current == null ? null : current.copy();
				net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer,
						new com.sighs.sophisticatedsorter.network.ClientboundContainerSettingsPayload(key, current));
			}
		}
	}

	@Override
	public void detectSettingsChangeAndReload() {
		if (player.level().isClientSide()) {
			// Server-side category edits (sent by the client via core's SyncContainerClientDataPayload)
			// update the server store; the server pushes the changed contents back into the client
			// mirror (ClientboundContainerSettingsPayload). Core calls this method every frame from
			// SettingsScreen.render, so reload only when the mirror actually changed.
			CompoundTag current = ClientContainerSettingsCache.getOrCreateContents(key);
			if (lastContentsNbt == null || !lastContentsNbt.equals(current)) {
				lastContentsNbt = current.copy();
				storageWrapper.getSettingsHandler().reloadFrom(
						ContainerSettingsHandler.fromNbt(current, player.level().registryAccess()));
			}
		}
	}
}