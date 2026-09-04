package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;

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
 * the container's current settings contents - the exact data {@link ContainerSettingsContainerMenu#fromBuffer}
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
		// the key, the real slot count, then the container's current settings contents (NeoForge
		// menu-type extra data) - the client rebuilds its wrapper from these, never from the server store.
		ServerContainerSettingsStore store = ServerContainerSettingsStore.get();
		CompoundTag contents = store == null ? new CompoundTag() : store.getOrCreateContents(key);
		// The client-side wrapper also needs the container's items: 26.1's settings menu storage slots
		// are ghost slots whose contents the client reads from its own wrapper handler (never synced
		// per-slot), so the opening data embeds the item snapshot under the inventory-snapshot tag. It
		// is only written into this outbound copy - the store record keeps settings only.
		CompoundTag outbound = withInventorySnapshot(contents, settingsProvider.wrapper(), player.level().registryAccess());
		player.openMenu(settingsProvider.provider(), buf -> {
			key.write(buf);
			buf.writeVarInt(settingsProvider.slots());
			buf.writeNbt(outbound);
		});
	}

	/** Embeds the wrapper's current item snapshot into a copy of the contents tag for the client. */
	private static CompoundTag withInventorySnapshot(CompoundTag contents, ContainerSettingsWrapper wrapper,
			HolderLookup.Provider registries) {
		CompoundTag outbound = contents.copy();
		InventoryHandler handler = wrapper.getInventoryHandler();
		int slots = handler.size();
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
		ListTag inventory = new ListTag(Tag.TAG_COMPOUND);
		for (int i = 0; i < slots; i++) {
			ItemStack.OPTIONAL_CODEC.encodeStart(ops, handler.getInternalStack(i)).result().ifPresent(inventory::add);
		}
		outbound.put(ContainerSettingsWrapper.INVENTORY_SNAPSHOT_TAG, inventory);
		return outbound;
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
			ServerContainerSettingsStore storage = ServerContainerSettingsStore.get();
			if (storage == null) {
				return null;
			}
			ContainerInventoryHandles.PlayerInventoryHandle realInventory =
					new ContainerInventoryHandles.PlayerInventoryHandle(player.getInventory());
			ContainerSettingsWrapper wrapper = ContainerSettingsWrapper.playerInventory(storage, key, realInventory,
					player.level().registryAccess());
			Component title = Component.translatable("gui.sophisticatedsorter.settings.player_inventory");
			MenuProvider provider = new SophisticatedMenuProvider(
					(windowId, inv, pl) -> ContainerSettingsContainerMenu.create(key, wrapper, storage, windowId, pl), title, false);
			return new SettingsProviderData(provider, wrapper.getInventoryHandler().size(), wrapper);
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
		ContainerSettingsWrapper wrapper = new ContainerSettingsWrapper(storage, key, slots, itemHandler, title,
				player.level().registryAccess());
		MenuProvider provider = new SophisticatedMenuProvider(
				(windowId, inv, pl) -> ContainerSettingsContainerMenu.create(key, wrapper, storage, windowId, pl), title, false);
		return new SettingsProviderData(provider, slots, wrapper);
	}

	private record SettingsProviderData(MenuProvider provider, int slots, ContainerSettingsWrapper wrapper) {
	}

	@Nullable
	private static MenuProvider findMenuProvider(ServerPlayer player, ContainerSettingsKey key) {
		ContainerTargetResolver.ContainerTarget target = ContainerTargetResolver.resolveTarget(player, key);
		return target != null ? target.blockEntity() : null;
	}
}