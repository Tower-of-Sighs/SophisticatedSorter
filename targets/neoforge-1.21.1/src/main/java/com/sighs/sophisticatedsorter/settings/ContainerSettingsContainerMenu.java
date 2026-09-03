package com.sighs.sophisticatedsorter.settings;

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
 * The stage-2 open flow (gear entry -> {@code ClientOpenContainerSettingsPayload}) swaps the
 * player's currently open container menu for this one. The server opens it through a
 * {@code SophisticatedMenuProvider} whose buffer carries the {@link ContainerSettingsKey} (with
 * which {@link #fromBuffer} reconstructs the exact same target) and the real slot count. The
 * screen title is not part of the buffer - like Sophisticated Backpacks' context stream, the
 * client derives the display name from the key, so the buffer stays small and registry-free.
 * <p>
 * On the client the wrapper is rebuilt from the stored settings nbt. For a block target there is
 * no live item handler on the client, so the write-through inventory stays empty - the settings
 * screen only shows the stored view of the container (the container's own screen is what the
 * player actually interacts with once they return to it).
 */
public class ContainerSettingsContainerMenu extends SettingsContainerMenu<ContainerSettingsWrapper> {
	private final ContainerSettingsStorage storage;
	private final ContainerSettingsKey key;
	/** Client-side copy of the last settings contents applied to this menu's handler. */
	private CompoundTag lastContentsNbt;

	public ContainerSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, ContainerSettingsWrapper storageWrapper,
			ContainerSettingsStorage storage, ContainerSettingsKey key) {
		super(menuType, windowId, player, storageWrapper);
		this.storage = storage;
		this.key = key;
	}

	/** Server-side factory for a real container target. */
	public static ContainerSettingsContainerMenu create(ContainerSettingsKey key, ContainerSettingsWrapper wrapper, int windowId, Player player) {
		return new ContainerSettingsContainerMenu(ModMenus.CONTAINER_SETTINGS.get(), windowId, player, wrapper, ContainerSettingsStorage.get(), key);
	}

	/**
	 * Rebuilds the menu on the client from the opening payload written by the server-side open
	 * helper (key, then real slot count). Mirrors {@code BackpackSettingsContainerMenu.fromBuffer}
	 * which reconstructs its context from the buffer.
	 */
	public static ContainerSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
		ContainerSettingsKey key = ContainerSettingsKey.fromBuffer(buf);
		int slots = buf.readVarInt();
		ContainerSettingsStorage storage = ContainerSettingsStorage.get();
		ContainerSettingsWrapper wrapper = key.isPlayerInventory()
				? ContainerSettingsWrapper.playerInventory(storage, key,
						new ContainerInventoryHandles.PlayerInventoryHandle(playerInventory.player.getInventory()))
				: new ContainerSettingsWrapper(storage, key, slots, null, Component.literal("container"));
		return new ContainerSettingsContainerMenu(ModMenus.CONTAINER_SETTINGS.get(), windowId, playerInventory.player, wrapper, storage, key);
	}

	public ContainerSettingsKey getKey() {
		return key;
	}

	public ContainerSettingsStorage getStorage() {
		return storage;
	}

	@Override
	public void detectSettingsChangeAndReload() {
		if (player.level().isClientSide) {
			// Single-process (integrated server) setup: client and server share the same
			// ContainerSettingsStorage instance and therefore the same live contents tag. Server-side
			// category edits (sent by the client via core's SyncContainerClientDataPayload) write into
			// that shared tag; the client's own category objects are stale until reloaded. Core calls
			// this method every frame from SettingsScreen.render, so reload only when the shared tag
			// actually changed (mirrors BackpackSettingsContainerMenu's lastSettingsNbt comparison).
			CompoundTag current = storage.getOrCreateContents(key);
			if (lastContentsNbt == null || !lastContentsNbt.equals(current)) {
				lastContentsNbt = current.copy();
				storageWrapper.getSettingsHandler().reloadFrom(current);
			}
		}
	}
}
