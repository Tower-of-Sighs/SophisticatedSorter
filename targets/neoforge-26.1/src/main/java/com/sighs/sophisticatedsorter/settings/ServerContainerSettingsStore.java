package com.sighs.sophisticatedsorter.settings;

import com.mojang.serialization.Codec;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Server-side authoritative persistence for the per-container settings of arbitrary containers.
 * <p>
 * Settings live in the world's save (a {@link SavedData} attached to the overworld's data storage),
 * so they follow the server world instead of the client's config folder - the previous design wrote
 * files under the client config directory and relied on the integrated server sharing the same
 * process/filesystem. Each record is keyed by {@link ContainerSettingsKey#toStorageKey()}, which
 * carries the dimension for block-entity containers (so the same coordinates in different
 * dimensions never collide) and a fixed key for the player inventory.
 * <p>
 * All mutations run on the server thread and mark the store dirty so the world save persists them.
 * The client never touches this store: it receives copies through the settings payloads and keeps
 * its own mirror ({@code com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache}).
 * <p>
 * 26.1 differences from the 1.21.1 implementation: {@code SavedData} moved to a codec-based
 * {@link SavedDataType} registration ({@link #TYPE}), so the contents map is serialized with a
 * {@link Codec} instead of the old {@link #save}/{@code load} pair, and the thread-group guard was
 * dropped (every caller is server-side; the null server check suffices).
 */
public class ServerContainerSettingsStore extends SavedData implements ContainerSettingsStore {
	private static final String SAVED_DATA_ID_PATH = "container_settings";

	private static final Codec<ServerContainerSettingsStore> CODEC =
			Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC)
					.xmap(ServerContainerSettingsStore::new, store -> store.contentsByKey);

	public static final SavedDataType<ServerContainerSettingsStore> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(SophisticatedSorter.MODID, SAVED_DATA_ID_PATH),
			ServerContainerSettingsStore::new, CODEC);

	private final Map<String, CompoundTag> contentsByKey = new HashMap<>();

	private ServerContainerSettingsStore() {
	}

	private ServerContainerSettingsStore(Map<String, CompoundTag> contentsByKey) {
		this.contentsByKey.putAll(contentsByKey);
	}

	/** The server-side store, resolved lazily from the current server's overworld data storage. */
	@Nullable
	public static ServerContainerSettingsStore get() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) {
			return null;
		}
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			return null;
		}
		SavedDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(TYPE);
	}

	/** Registry access of the world the store is attached to (needed to (de)serialize settings). */
	@Nullable
	public net.minecraft.core.HolderLookup.Provider registryAccess() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) {
			return null;
		}
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		return overworld == null ? null : overworld.registryAccess();
	}

	/** Returns the live, cached contents for the key, creating an empty record when first touched. */
	public CompoundTag getOrCreateContents(ContainerSettingsKey key) {
		return contentsByKey.computeIfAbsent(key.toStorageKey(), k -> new CompoundTag());
	}

	/** Returns the stored contents for the key, or null when no record exists. */
	@Nullable
	public CompoundTag getContents(ContainerSettingsKey key) {
		return contentsByKey.get(key.toStorageKey());
	}

	/** Replaces the stored contents for the key and marks the store dirty. */
	public void saveContents(ContainerSettingsKey key, CompoundTag contents) {
		contentsByKey.put(key.toStorageKey(), contents);
		setDirty();
	}

	/** Removes the record for the key and marks the store dirty. */
	public void remove(ContainerSettingsKey key) {
		if (contentsByKey.remove(key.toStorageKey()) != null) {
			setDirty();
		}
	}

	/** Marks the store dirty so the world save persists it (called after a settings edit). */
	public void markDirty(ContainerSettingsKey key) {
		setDirty();
	}
}