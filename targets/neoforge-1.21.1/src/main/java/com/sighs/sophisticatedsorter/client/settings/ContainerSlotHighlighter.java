package com.sighs.sophisticatedsorter.client.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.settings.ISlotColorCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsHandler;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsStorage;

/**
 * Client-side reader of the per-slot settings of the container the player currently has open. The
 * server resolves the open menu to a {@link ContainerSettingsKey} (its slots reference the real
 * block entity) and pushes it to {@link ClientTrackedContainer}; this reader builds a settings
 * handler over the shared settings storage for that key, so vanilla container screens can draw the
 * same slot highlights the settings screen shows.
 * <p>
 * Only the settings categories are read - the handler is built over the shared contents tag with
 * lazy suppliers that are never dereferenced for color reads, so this is cheap and has no side
 * effects on the real inventory.
 */
public final class ContainerSlotHighlighter {
	private ContainerSlotHighlighter() {
	}

	/** Settings handler for the currently tracked container, or null when none is open / resolvable. */
	@Nullable
	public static SettingsHandler settingsForTrackedContainer() {
		ContainerSettingsKey key = ClientTrackedContainer.getCurrentKey();
		if (key == null || key.isPlayerInventory()) {
			return null;
		}
		return new ContainerSettingsHandler(
				ContainerSettingsStorage.get().getOrCreateContents(key),
				() -> {},
				() -> null,
				() -> null);
	}

	/**
	 * Colors to stripe over the given storage slot, in the same form Core's storage screens draw
	 * (diffuse color with the semi-transparent overlay alpha). Empty when the slot has no highlight.
	 */
	public static List<Integer> overlayColors(SettingsHandler settings, int slotNumber) {
		List<Integer> ret = new ArrayList<>();
		for (ISlotColorCategory category : settings.getCategoriesThatImplement(ISlotColorCategory.class)) {
			category.getSlotColor(slotNumber).ifPresent(color -> ret.add(color & 0xFFFFFF | 0x50000000));
		}
		return ret;
	}

	/**
	 * The memorized stack of the given storage slot of the tracked container, or empty when the slot
	 * is not memorized. Mirrors how Core's storage menu resolves the ghost: {@code getSlotFilterStack}
	 * returns the remembered item (or stack) for the slot, and the caller renders it only while the
	 * slot itself is empty.
	 */
	public static Optional<ItemStack> memorizedStack(MemorySettingsCategory memory, int slotNumber) {
		return memory.getSlotFilterStack(slotNumber, true);
	}
}
