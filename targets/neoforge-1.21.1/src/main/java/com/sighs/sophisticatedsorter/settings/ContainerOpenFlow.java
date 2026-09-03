package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;

/**
 * Server-side open/close flow for the container-settings screen.
 * <p>
 * Both directions are server-driven menu swaps, mirroring how Sophisticated Core opens its
 * settings screen from a storage block (the server swaps the player's menu):
 * <ul>
 * <li>{@link #openSettings}: resolves the target block entity from the {@link ContainerSettingsKey}
 * (falling back to the tracker when the key was not recorded), records the key as the player's
 * "currently managed container", and opens the settings menu with a
 * {@link SophisticatedMenuProvider} whose opening buffer carries the key, the real slot count and
 * the container's display title - the exact data {@link ContainerSettingsContainerMenu#fromBuffer}
 * needs to rebuild the wrapper and screen on the client.</li>
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
		// The opening buffer must carry exactly what ContainerSettingsContainerMenu.fromBuffer reads:
		// the key, then the real slot count (NeoForge menu-type extra data).
		player.openMenu(settingsProvider.provider(), buf -> {
			key.write(buf);
			buf.writeVarInt(settingsProvider.slots());
		});
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
		// Wrap the block entity's provider so the swap does NOT trigger a client-side container close:
		// the vanilla default returns true, which makes the client close the settings screen (releasing
		// the mouse to the center of the screen) before reopening the container. SophisticatedMenuProvider
		// with trigger=false swaps menus without that client-side close, exactly like the settings-open
		// path and like Sophisticated Backpacks' own menu swaps.
		player.openMenu(new SophisticatedMenuProvider(
				originalProvider::createMenu, originalProvider.getDisplayName(), false));
	}

	@Nullable
	private static SettingsProviderData createSettingsProvider(ServerPlayer player, ContainerSettingsKey key) {
		if (key.isPlayerInventory()) {
			ContainerSettingsStorage storage = ContainerSettingsStorage.get();
			ContainerInventoryHandles.PlayerInventoryHandle realInventory =
					new ContainerInventoryHandles.PlayerInventoryHandle(player.getInventory());
			ContainerSettingsWrapper wrapper = ContainerSettingsWrapper.playerInventory(storage, key, realInventory);
			Component title = Component.translatable("gui.sophisticatedsorter.settings.player_inventory");
			MenuProvider provider = new SophisticatedMenuProvider(
					(windowId, inv, pl) -> ContainerSettingsContainerMenu.create(key, wrapper, windowId, pl), title, false);
			return new SettingsProviderData(provider, wrapper.getInventoryHandler().getSlots());
		}
		ContainerTargetResolver.ContainerTarget target = ContainerTargetResolver.resolveTarget(player, key);
		if (target == null) {
			return null;
		}
		IItemHandlerModifiable itemHandler = target.itemHandler();
		int slots = target.slots();
		Component title = target.title();
		ContainerSettingsStorage storage = ContainerSettingsStorage.get();
		ContainerSettingsWrapper wrapper = new ContainerSettingsWrapper(storage, key, slots, itemHandler, title);
		MenuProvider provider = new SophisticatedMenuProvider(
				(windowId, inv, pl) -> ContainerSettingsContainerMenu.create(key, wrapper, windowId, pl), title, false);
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
