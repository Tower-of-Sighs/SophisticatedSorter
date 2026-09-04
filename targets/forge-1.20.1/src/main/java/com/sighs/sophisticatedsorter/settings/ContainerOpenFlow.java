package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.network.ClientboundOpenContainerSettingsPayload;
import com.sighs.sophisticatedsorter.network.NetworkHandler;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Server-side open/close flow for the container-settings screen.
 * <p>
 * Both directions are server-driven menu swaps, mirroring how Sophisticated Core opens its
 * settings screen from a storage block (the server swaps the player's menu):
 * <ul>
 * <li>{@link #openSettings}: resolves the target block entity from the {@link ContainerSettingsKey}
 * (falling back to the tracker when the key was not recorded), records the key as the player's
 * "currently managed container", and opens the settings menu through a vanilla {@link MenuProvider}.
 * Forge 1.20.1 has no menu-open extra data (the client menu constructor receives no buffer), so the
 * exact data {@link ContainerSettingsContainerMenu#fromBuffer} needs - the key, the real slot count
 * and the container's current settings contents - is pushed over the mod's own channel first
 * ({@link ClientboundOpenContainerSettingsPayload}, which also feeds the client-side mirror
 * {@code ClientContainerSettingsCache}).</li>
 * <li>{@link #closeSettings}: reopens the container the player came from by looking up the
 * recorded block entity and calling {@code openMenu} with its own {@link MenuProvider}. If the
 * target is gone or was never recorded the vanilla close is used instead.</li>
 * </ul>
 * Opening the settings menu replaces the currently open container menu server-side (the vanilla
 * container screen is dismissed on the client); reopening the same block entity's provider from
 * the return path restores it.
 */
public final class ContainerOpenFlow {
	private ContainerOpenFlow() {
	}

	/**
	 * Opens the settings menu for the given key, building the settings wrapper over the live item
	 * handler of the target block entity on the server.
	 */
	public static void openSettings(ServerPlayer player, ContainerSettingsKey key) {
		SettingsProviderData settingsProvider = createSettingsProvider(player, key);
		if (settingsProvider == null) {
			SophisticatedSorter.LOGGER.warn("Cannot open container settings for {} - target not found", key);
			return;
		}
		// Record the return target BEFORE the menu swap: opening the settings menu fires a
		// container-close event that clears the "currently open" key, and the return flow needs the
		// original container to reopen when the player leaves the settings screen.
		ContainerSettingsTracker.get().setReturnKey(player, key);
		// The client must rebuild the wrapper from exactly the data ContainerSettingsContainerMenu
		// reads: the key, the real slot count, then the container's current settings contents. Forge
		// 1.20.1 cannot carry this data through the menu-open screen packet, so it travels over the
		// mod channel ahead of the open packet (ordered on the same connection).
		ServerContainerSettingsStore store = ServerContainerSettingsStore.get();
		CompoundTag contents = store == null ? new CompoundTag() : store.getOrCreateContents(key);
		NetworkHandler.sendToClient(player,
				new ClientboundOpenContainerSettingsPayload(key, settingsProvider.slots(), contents));
		closeCurrentMenuWithoutClientNotification(player);
		player.openMenu(settingsProvider.provider());
	}

	/**
	 * Closes the settings screen and reopens the container the player was viewing before it. The
	 * return key recorded when settings opened identifies the original target.
	 */
	public static void closeSettings(ServerPlayer player) {
		ContainerSettingsKey key = ContainerSettingsTracker.get().getAndClearReturnKey(player);
		if (key == null || key.isPlayerInventory()) {
			// No recorded original block, or the original was the player inventory main screen:
			// vanilla close returns the player to their inventory screen.
			player.closeContainer();
			return;
		}
		MenuProvider originalProvider = findMenuProvider(player, key);
		if (originalProvider == null) {
			// The block the player came from is gone; just close back to the inventory screen.
			player.closeContainer();
			return;
		}
		// Wrap the block entity's provider so the swap does NOT close the client's screen: closing the
		// settings screen would release the mouse, and the immediately following container open would
		// re-grab it (jumping the cursor to the screen center). Closing the current menu server-side
		// only (doCloseContainer) lets the client swap screens in place.
		closeCurrentMenuWithoutClientNotification(player);
		player.openMenu(new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player pl) {
				return originalProvider.createMenu(windowId, inv, pl);
			}

			@Override
			public Component getDisplayName() {
				return originalProvider.getDisplayName();
			}
		});
	}

	/**
	 * Closes the player's currently open container menu server-side only, without telling the client
	 * to close its screen. Forge 1.20.1's vanilla {@code ServerPlayer.openMenu} otherwise closes the
	 * active container first ({@code closeContainer()}), which sends a container-close packet - the
	 * client then releases the mouse and re-grabs it when the next open-screen packet arrives, jumping
	 * the cursor to the screen center on every settings open/return. This mirrors the reference's
	 * {@code SophisticatedMenuProvider(..., trigger = false)} path on 1.21.1 (its patched
	 * {@code openMenu} calls {@code doCloseContainer()} instead of {@code closeContainer()} for such
	 * providers); after it runs, {@code openMenu} sees the player back on the inventory menu and skips
	 * its own client-visible close.
	 */
	private static void closeCurrentMenuWithoutClientNotification(ServerPlayer player) {
		if (player.containerMenu != player.inventoryMenu) {
			player.doCloseContainer();
		}
	}

	@Nullable
	private static SettingsProviderData createSettingsProvider(ServerPlayer player, ContainerSettingsKey key) {
		if (key.isPlayerInventory()) {
			ServerContainerSettingsStore storage = ServerContainerSettingsStore.get();
			if (storage == null) {
				return null;
			}
			ContainerInventoryHandles.PlayerInventoryHandle realInventory =
					new ContainerInventoryHandles.PlayerInventoryHandle(player.getInventory());
			ContainerSettingsWrapper wrapper = ContainerSettingsWrapper.playerInventory(storage, key, realInventory);
			Component title = Component.translatable("gui.sophisticatedsorter.settings.player_inventory");
			MenuProvider provider = new MenuProvider() {
				@Override
				public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player pl) {
					return ContainerSettingsContainerMenu.create(key, wrapper, storage, windowId, pl);
				}

				@Override
				public Component getDisplayName() {
					return title;
				}
			};
			return new SettingsProviderData(provider, wrapper.getInventoryHandler().getSlots());
		}
		ContainerTargetResolver.ContainerTarget target = ContainerTargetResolver.resolveTarget(player, key);
		if (target == null) {
			return null;
		}
		IItemHandlerModifiable itemHandler = target.itemHandler();
		int slots = target.slots();
		Component title = target.title();
		ServerContainerSettingsStore storage = ServerContainerSettingsStore.get();
		if (storage == null) {
			return null;
		}
		ContainerSettingsWrapper wrapper = new ContainerSettingsWrapper(storage, key, slots, itemHandler, title);
		MenuProvider provider = new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player pl) {
				return ContainerSettingsContainerMenu.create(key, wrapper, storage, windowId, pl);
			}

			@Override
			public Component getDisplayName() {
				return title;
			}
		};
		return new SettingsProviderData(provider, slots);
	}

	private record SettingsProviderData(MenuProvider provider, int slots) {
	}

	@Nullable
	private static MenuProvider findMenuProvider(ServerPlayer player, ContainerSettingsKey key) {
		ContainerTargetResolver.ContainerTarget target = ContainerTargetResolver.resolveTarget(player, key);
		return target != null ? target.blockEntity() : null;
	}
}