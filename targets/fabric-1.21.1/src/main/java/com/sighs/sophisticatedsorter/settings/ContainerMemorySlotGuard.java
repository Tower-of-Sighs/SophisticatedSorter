package com.sighs.sophisticatedsorter.settings;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;

/**
 * Server-side gate that rejects placements into memory-filtered slots of vanilla container menus.
 * <p>
 * Sophisticated Core's memory filters are normally enforced by core's own {@code InventoryHandler}:
 * {@code isItemValid} ends with {@code memory.matchesFilter(slot, stack)} and core's storage slots
 * delegate {@code mayPlace} to that check. Vanilla container slots have no such hook, so when this
 * mod stores memory settings for a vanilla block-entity container (chest, etc.) nothing would stop
 * a player from dropping an item that does not match the slot's memory filter into it. This class
 * makes {@link net.minecraft.world.inventory.Slot#mayPlace(ItemStack)} reject those placements,
 * mirroring core's semantics: a placement is allowed when the slot has no memory filter or the
 * stack matches it, and rejected otherwise.
 * <p>
 * <b>How the gate knows it is on the server:</b> {@link ContainerMemorySlotMixin} arms the guard
 * around the server-side container click ({@code ServerGamePacketListenerImpl.handleContainerClick},
 * see {@link #arm} / {@link #disarm}), which runs only on the server thread and carries the acting
 * {@link ServerPlayer}. {@code Slot.mayPlace} itself has no player reference, and in a single-player
 * integrated server the client and the server share the same menu object, so the guard cannot infer
 * the side from the menu or the slot. On the client the same {@code AbstractContainerMenu.clicked} /
 * {@code quickMoveStack} calls run for prediction without the guard armed, so nothing is rejected
 * there (rejecting prediction would only desynchronize the local {@code SimpleContainer} copies -
 * the authoritative rejection happens on the server, where the menu's storage slots reference the
 * real block entity).
 * <p>
 * <b>Scope of the gate:</b>
 * <ul>
 * <li>Only the storage slots of the <i>tracked vanilla container menu</i> are gated. The tracker
 * records the key of the container the player's menu resolved to when the menu opened (on NeoForge
 * through {@code PlayerContainerEvent.Open}, on this target through the
 * {@code ServerPlayer.openMenu} injection), and the gate only applies to slots whose container is
 * not the player {@link Inventory} (main, armor, offhand).</li>
 * <li>Core {@code StorageContainerMenuBase} and {@code SettingsContainerMenu} menus are skipped:
 * their storage slots already enforce the filters through core's {@code InventoryHandler}, and the
 * settings screen's slots are view-only over a wrapper whose indexes would not line up with the
 * real container.</li>
 * <li>Non-player-driven insertions (hoppers, pipes) never call {@code Slot.mayPlace}; they are
 * deliberately not gated, exactly as a vanilla container's {@code isItemValid} is not consulted by
 * them.</li>
 * </ul>
 */
public final class ContainerMemorySlotGuard {
	/**
	 * Upper bound of the slot indexes the per-single-block memory settings can cover. A double chest
	 * menu has 54 storage slots but the settings record covers one half (27 slots); only indexes the
	 * settings handler can actually have a filter for are worth consulting. This is generous (54) and
	 * relies on the memory category only ever containing 0..26 of a half, so nothing beyond is ever
	 * rejected.
	 */
	private static final int MAX_SETTINGS_SLOTS = 54;

	private static final ThreadLocal<ContainerMemorySlotGuard> GUARD = ThreadLocal.withInitial(ContainerMemorySlotGuard::new);

	private ContainerMemorySlotGuard() {
	}

	/**
	 * Arms the gate for the given server player. Called from the {@code handleContainerClick} HEAD
	 * injection; the guard stays armed until {@link #disarm} runs at the method's RETURN. Overwrites
	 * any previous state unconditionally so a frame leaked by an exception (whose RETURN disarm never
	 * ran) self-heals on the next click.
	 */
	public static void arm(ServerPlayer player) {
		ContainerMemorySlotGuard guard = GUARD.get();
		guard.armed = 1;
		guard.player = player;
		guard.menu = player.containerMenu;
		guard.key = null;
	}

	/** Disarms the gate (see {@link #arm}). */
	public static void disarm() {
		ContainerMemorySlotGuard guard = GUARD.get();
		guard.armed = 0;
		guard.player = null;
		guard.menu = null;
		guard.key = null;
	}

	/**
	 * Whether the mixin must reject the placement into the slot because it targets a memorized slot
	 * of the tracked vanilla container whose filter does not match the stack.
	 *
	 * @return {@code true} to reject (memory mismatch), {@code false} to allow, or {@code null} when
	 *         the check does not apply (no server click on the stack, not the tracked container, not
	 *         a vanilla-container storage slot, no memory filter on the slot, ...) and the vanilla
	 *         {@code mayPlace} result should stand.
	 */
		public static Boolean rejectIfMemoryMismatch(Slot slot, ItemStack stack) {
		if (stack.isEmpty() || isPlayerInventorySlot(slot)) {
			return null;
		}
		ContainerMemorySlotGuard guard = GUARD.get();
		if (guard.armed == 0 || guard.player == null || guard.menu == null) {
			return null;
		}
		ServerPlayer player = guard.player;
		if (player.hasDisconnected()) {
			return null;
		}
		// Only the menu the guard was armed for. The tracker records exactly the menu whose storage
		// container resolved to the block entity on open, and the player's menu can only have been
		// swapped by another open (which would have disarmed/re-armed this guard on the next click).
		AbstractContainerMenu menu = player.containerMenu;
		if (menu == null || menu != guard.menu) {
			return null;
		}
		if (isCoreMenu(menu)) {
			return null;
		}
		if (guard.key == null) {
			guard.key = ContainerSettingsTracker.get().getOpenKey(player);
		}
		if (guard.key == null || guard.key.isPlayerInventory()) {
			return null;
		}
		if (slot.index < 0 || slot.index >= MAX_SETTINGS_SLOTS) {
			return null;
		}
		MemorySettingsCategory memory = memoryCategory(guard.key);
		if (memory == null) {
			return null;
		}
		// matchesFilter returns true when the slot is not memorized OR the stack matches the memorized
		// item; only an actual mismatch (false) is rejected.
		return memory.matchesFilter(slot.index, stack) ? null : Boolean.FALSE;
	}

	/** Slots backing the player inventory (main, armor, offhand) never carry container memory filters. */
	private static boolean isPlayerInventorySlot(Slot slot) {
		return slot.container instanceof Inventory;
	}

	/** Core storage / settings screens already enforce the memory filters through their own handler. */
	private static boolean isCoreMenu(AbstractContainerMenu menu) {
		return menu instanceof StorageContainerMenuBase || menu instanceof SettingsContainerMenu;
	}

		private static MemorySettingsCategory memoryCategory(ContainerSettingsKey key) {
		// The settings handler is built lazily per call; it only deserializes the small settings nbt of
		// the tracked container (the memory tab does the same on every screen rebuild). The inventory-
		// handler and render-info suppliers are never dereferenced by a memory read.
		ContainerSettingsStore storage = ServerContainerSettingsStore.get();
		if (storage == null) {
			return null;
		}
		ContainerSettingsHandler handler = new ContainerSettingsHandler(
				storage.getOrCreateContents(key),
				() -> {},
				() -> null,
				() -> null);
		return handler.getTypeCategory(MemorySettingsCategory.class);
	}

	/**
	 * Clears any stale armed state so a leftover guard can never gate a later menu. Called from the
	 * {@code AbstractContainerMenu.removed} injection when a menu closes server-side (the NeoForge
	 * reference registers a {@code PlayerContainerEvent.Close} listener for the same purpose).
	 */
	public static void onContainerClosed() {
		GUARD.get().clear();
	}

	private void clear() {
		armed = 0;
		player = null;
		menu = null;
		key = null;
	}

	private int armed;
		private ServerPlayer player;
		private AbstractContainerMenu menu;
		private ContainerSettingsKey key;
}