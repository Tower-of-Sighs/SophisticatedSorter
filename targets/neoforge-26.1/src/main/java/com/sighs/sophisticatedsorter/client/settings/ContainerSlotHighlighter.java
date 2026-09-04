package com.sighs.sophisticatedsorter.client.settings;

import com.sighs.sophisticatedsorter.settings.ContainerSettingsHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.settings.ISlotColorCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;

/**
 * Client-side reader of the per-slot settings categories of a container, given its settings contents
 * tag. The tag is the client mirror of the server-owned settings
 * ({@link ClientContainerSettingsCache}), pushed by the server when a container is opened or its
 * settings change; the caller (the container-screen decoration mixin) passes the tag for the
 * currently open container so vanilla screens can draw the same slot highlights the settings screen
 * shows.
 * <p>
 * Only the settings categories are read - the handler is built over the decoded settings data with
 * lazy suppliers that are never dereferenced for color reads, so this is cheap and has no side
 * effects.
 * <p>
 * 26.1 difference from the 1.21.1 implementation: core's settings model now decodes through
 * {@link ContainerSettingsHandler#fromNbt}, which needs a registry-aware codec, so the caller must
 * pass the registry access of the current level.
 */
public final class ContainerSlotHighlighter {
	private ContainerSlotHighlighter() {
	}

	/** Settings handler over the given contents tag (no side effects on any store). */
	@Nullable
	public static SettingsHandler settingsForContents(CompoundTag contents, HolderLookup.Provider registries) {
		return new ContainerSettingsHandler(
				ContainerSettingsHandler.fromNbt(contents, registries),
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