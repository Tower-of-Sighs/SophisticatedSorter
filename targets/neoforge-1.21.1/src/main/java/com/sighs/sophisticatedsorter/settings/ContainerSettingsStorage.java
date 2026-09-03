package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;

/**
 * Filesystem persistence for the per-container settings of arbitrary containers.
 * <p>
 * The root is NeoForge's client config directory resolved through {@code FMLPaths.CONFIGDIR}. The
 * same directory is reachable from the client and the integrated-server thread, so settings edited
 * while a world is open persist under the client's config folder exactly like the mod's client
 * config does.
 * <p>
 * Layout under the root: {@code sophisticatedsorter/container-settings/<dim-namespace>/<dim-path>/<x>_<y>_<z>.dat}
 * for a block target and {@code .../player_inventory.dat} for the player inventory.
 * <p>
 * A live {@link CompoundTag} is cached per key. It is written back to disk whenever
 * {@link #markDirty} is invoked (which is what core's save chain does after a settings edit), so
 * this stays a plain server-thread store: no locking beyond the map itself.
 */
public final class ContainerSettingsStorage {
	private static final String ROOT_NAME = "container-settings";

	private static ContainerSettingsStorage instance;

	private final Path rootDir;
	private final Map<ContainerSettingsKey, Entry> contentsByKey = new HashMap<>();

	private ContainerSettingsStorage(Path rootDir) {
		this.rootDir = rootDir;
	}

	public static ContainerSettingsStorage get() {
		ContainerSettingsStorage localInstance = instance;
		if (localInstance == null) {
			localInstance = instance = create(FMLLoader.getGamePath().resolve("config"));
		}
		return localInstance;
	}

	/** Test/seam hook: build a storage rooted at an arbitrary directory. */
	public static ContainerSettingsStorage create(Path rootDir) {
		return new ContainerSettingsStorage(rootDir);
	}

	public Path getRootDir() {
		return rootDir;
	}

	/** Returns the live, cached contents for the key, loading the file when it is first touched. */
	public CompoundTag getOrCreateContents(ContainerSettingsKey key) {
		return contentsByKey.computeIfAbsent(key, this::load).tag;
	}

	/** Replaces the live contents for the key and persists them immediately. */
	public void saveContents(ContainerSettingsKey key, CompoundTag tag) {
		contentsByKey.put(key, new Entry(tag));
		write(key);
	}

	/** Removes the key from the cache and deletes its backing file. */
	public void remove(ContainerSettingsKey key) {
		contentsByKey.remove(key);
		try {
			Files.deleteIfExists(getFile(key));
		} catch (IOException e) {
			SophisticatedSorter.LOGGER.error("Failed to delete settings file for {}", key, e);
		}
	}

	/**
	 * Writes the cached contents of the key to disk. Called by the settings save chain whenever a
	 * category is edited.
	 */
	public void markDirty(ContainerSettingsKey key) {
		Entry entry = contentsByKey.get(key);
		if (entry != null) {
			write(key);
		}
	}

	private Entry load(ContainerSettingsKey key) {
		Path file = getFile(key);
		if (!Files.isRegularFile(file)) {
			return new Entry(new CompoundTag());
		}
		try {
			return new Entry(NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()));
		} catch (IOException e) {
			SophisticatedSorter.LOGGER.error("Failed to load settings file {} - starting with empty settings", file, e);
			return new Entry(new CompoundTag());
		}
	}

	private void write(ContainerSettingsKey key) {
		Entry entry = contentsByKey.get(key);
		if (entry == null) {
			return;
		}
		Path file = getFile(key);
		try {
			Files.createDirectories(file.getParent());
			NbtIo.writeCompressed(entry.tag, file);
		} catch (IOException e) {
			SophisticatedSorter.LOGGER.error("Failed to save settings to {}", file, e);
		}
	}

	private Path getFile(ContainerSettingsKey key) {
		if (key.isPlayerInventory()) {
			return rootDir.resolve(SophisticatedSorter.MODID).resolve(ROOT_NAME).resolve(ContainerSettingsKey.PLAYER_INVENTORY_FILE + ".dat");
		}
		ResourceLocation dimId = key.getDimension().location();
		Path posPath = rootDir.resolve(SophisticatedSorter.MODID).resolve(ROOT_NAME).resolve(dimId.getNamespace()).resolve(dimId.getPath());
		// resolve(...) would treat Windows drive letters in the dim path as absolute roots, so concat manually
		return posPath.resolveSibling(key.getPos().getX() + "_" + key.getPos().getY() + "_" + key.getPos().getZ() + ".dat");
	}

	private static final class Entry {
		private final CompoundTag tag;

		private Entry(CompoundTag tag) {
			this.tag = tag;
		}
	}
}
