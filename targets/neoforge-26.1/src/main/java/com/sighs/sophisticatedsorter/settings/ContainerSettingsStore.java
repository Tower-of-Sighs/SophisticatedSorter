package com.sighs.sophisticatedsorter.settings;

import net.minecraft.nbt.CompoundTag;

/**
 * Read/write access to the settings contents of one container key. Implemented server-side by
 * {@link ServerContainerSettingsStore} (SavedData, authoritative) and client-side by
 * {@link com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache} (mirror of the
 * server-pushed contents). The settings wrapper and menu only depend on this seam. 26.1 port of the
 * shared {@code common/src/minecraft} interface.
 */
public interface ContainerSettingsStore {
	/** Returns the live, cached contents for the key, creating an empty record when first touched. */
	CompoundTag getOrCreateContents(ContainerSettingsKey key);

	/** Marks the contents of the key dirty so the store persists it (no-op for the client mirror). */
	void markDirty(ContainerSettingsKey key);
}