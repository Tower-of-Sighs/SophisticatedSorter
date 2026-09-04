package com.sighs.sophisticatedsorter.settings;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.util.datafix.DataFixTypes;

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
 * Fabric has no equivalent of NeoForge's {@code SidedThreadGroups}/{@code ServerLifecycleHooks}, so
 * the currently running server is tracked through {@link ServerLifecycleEvents} (see
 * {@link #registerLifecycleHandlers()}) and all reads of this store happen on the server thread
 * anyway (payload handlers and mixins run there).
 */
public class ServerContainerSettingsStore extends SavedData implements ContainerSettingsStore {
	private static final String SAVED_DATA_NAME = "sophisticatedsorter_container_settings";
	private static final String CONTENTS_LIST_TAG = "contents";

	private static volatile MinecraftServer currentServer;

	private final Map<String, CompoundTag> contentsByKey = new HashMap<>();

	private ServerContainerSettingsStore() {
	}

	private ServerContainerSettingsStore(Map<String, CompoundTag> contentsByKey) {
		this.contentsByKey.putAll(contentsByKey);
	}

	/** Tracks the running server so {@link #get()} can reach its overworld data storage. */
	public static void registerLifecycleHandlers() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> currentServer = null);
	}

	/** The server-side store, resolved lazily from the current server's overworld data storage. */
		public static ServerContainerSettingsStore get() {
		MinecraftServer server = currentServer;
		if (server != null) {
			ServerLevel overworld = server.getLevel(Level.OVERWORLD);
			if (overworld != null) {
				DimensionDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(
						new Factory<>(ServerContainerSettingsStore::new, ServerContainerSettingsStore::load, DataFixTypes.SAVED_DATA_MAP_DATA),
						SAVED_DATA_NAME);
			}
		}
		return null;
	}

	/** Returns the live, cached contents for the key, creating an empty record when first touched. */
	public CompoundTag getOrCreateContents(ContainerSettingsKey key) {
		return contentsByKey.computeIfAbsent(key.toStorageKey(), k -> new CompoundTag());
	}

	/** Returns the stored contents for the key, or null when no record exists. */
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

	@Override
	public CompoundTag save(CompoundTag tag, Provider registries) {
		ListTag contentsList = new ListTag();
		for (Map.Entry<String, CompoundTag> entry : contentsByKey.entrySet()) {
			CompoundTag record = new CompoundTag();
			record.putString("key", entry.getKey());
			record.put("contents", entry.getValue());
			contentsList.add(record);
		}
		tag.put(CONTENTS_LIST_TAG, contentsList);
		return tag;
	}

	public static ServerContainerSettingsStore load(CompoundTag tag, Provider registries) {
		Map<String, CompoundTag> contents = new HashMap<>();
		ListTag contentsList = tag.getList(CONTENTS_LIST_TAG, Tag.TAG_COMPOUND);
		for (int i = 0; i < contentsList.size(); i++) {
			CompoundTag record = contentsList.getCompound(i);
			contents.put(record.getString("key"), record.getCompound("contents"));
		}
		return new ServerContainerSettingsStore(contents);
	}
}