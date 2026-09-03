package com.sighs.sophisticatedsorter.client.settings;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsStore;

/**
 * Client-side mirror of the per-container settings that the server owns. The server pushes each
 * container's settings contents to the client (via
 * {@link com.sighs.sophisticatedsorter.network.ClientboundContainerSettingsPayload}) whenever a
 * container is opened or its settings change; this cache is what the client-side highlights, memory
 * ghosts and the settings menu read from. The client never reads the server's SavedData directly.
 */
public final class ClientContainerSettingsCache {
	private static final Map<ContainerSettingsKey, CompoundTag> contentsByKey = new HashMap<>();

	/** A {@link ContainerSettingsStore} view over the static mirror (for wrapper/menu construction). */
	public static final ContainerSettingsStore STORE = new ContainerSettingsStore() {
		@Override
		public CompoundTag getOrCreateContents(ContainerSettingsKey key) {
			return contentsByKey.computeIfAbsent(key, k -> new CompoundTag());
		}

		@Override
		public void markDirty(ContainerSettingsKey key) {
			// The client mirror is fed by server pushes; there is nothing to persist locally.
		}
	};

	private ClientContainerSettingsCache() {
	}

	/** Returns the cached contents for the key, or an empty tag when none has been pushed yet. */
	public static CompoundTag getOrCreateContents(ContainerSettingsKey key) {
		return contentsByKey.computeIfAbsent(key, k -> new CompoundTag());
	}

	/** Returns the cached contents for the key, or null when none has been pushed. */
	@Nullable
	public static CompoundTag getContents(ContainerSettingsKey key) {
		return contentsByKey.get(key);
	}

	/** Stores the server-pushed contents for the key. */
	public static void putContents(ContainerSettingsKey key, CompoundTag contents) {
		contentsByKey.put(key, contents);
	}

	/** Removes the cached contents for the key (e.g. the container was closed). */
	public static void remove(ContainerSettingsKey key) {
		contentsByKey.remove(key);
	}

	/** Clears the whole cache (e.g. leaving the world). */
	public static void clear() {
		contentsByKey.clear();
	}
}
