package com.sighs.sophisticatedsorter.settings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Identifies the target of a per-container settings record: either a block-entity container
 * (dimension + position) or the player inventory.
 * <p>
 * Immutable value type. It is stream-encoded with a type byte followed by the dimension id and
 * position (block target) so that later stages can carry it inside open/close payloads. The
 * player-inventory target intentionally has no owning player: the settings are world-wide and the
 * same fixed key is used on client and integrated-server sides.
 * <p>
 * 26.1 port of the shared {@code common/src/minecraft} class: Minecraft 26.1 renamed
 * {@code ResourceLocation} to {@link Identifier}, so the wire methods use the 26.1 buffer names.
 */
public final class ContainerSettingsKey {
	private static final int TYPE_BLOCK = 0;
	private static final int TYPE_PLAYER_INVENTORY = 1;

	/** Fixed storage key under which the player-inventory settings are persisted. */
	public static final String PLAYER_INVENTORY_FILE = "player_inventory";

	private final ResourceKey<Level> dimension;
	private final BlockPos pos;
	private final boolean playerInventory;

	private ContainerSettingsKey(ResourceKey<Level> dimension, BlockPos pos, boolean playerInventory) {
		this.dimension = dimension;
		this.pos = pos;
		this.playerInventory = playerInventory;
	}

	public static ContainerSettingsKey block(ResourceKey<Level> dimension, BlockPos pos) {
		return new ContainerSettingsKey(dimension, pos, false);
	}

	public static ContainerSettingsKey playerInventory() {
		return new ContainerSettingsKey(null, null, true);
	}

	public boolean isPlayerInventory() {
		return playerInventory;
	}

	/** @throws IllegalStateException when this key targets the player inventory */
	public ResourceKey<Level> getDimension() {
		if (dimension == null) {
			throw new IllegalStateException("Player-inventory settings key has no dimension");
		}
		return dimension;
	}

	/** @throws IllegalStateException when this key targets the player inventory */
	public BlockPos getPos() {
		if (pos == null) {
			throw new IllegalStateException("Player-inventory settings key has no block position");
		}
		return pos;
	}

	public void write(FriendlyByteBuf buf) {
		if (playerInventory) {
			buf.writeByte(TYPE_PLAYER_INVENTORY);
		} else {
			buf.writeByte(TYPE_BLOCK);
			buf.writeIdentifier(dimension.identifier());
			buf.writeBlockPos(pos);
		}
	}

	public static ContainerSettingsKey fromBuffer(FriendlyByteBuf buf) {
		return buf.readByte() == TYPE_PLAYER_INVENTORY ? playerInventory()
				: block(ResourceKey.create(Registries.DIMENSION, buf.readIdentifier()), buf.readBlockPos());
	}

	/**
	 * Stable string key used in the server-side SavedData store. Block targets are
	 * {@code "block:<namespace>:<path>:<x>_<y>_<z>"} (the dimension is included so two dimensions'
	 * containers never collide), the player inventory is {@value #PLAYER_INVENTORY_FILE}.
	 */
	public String toStorageKey() {
		if (playerInventory) {
			return PLAYER_INVENTORY_FILE;
		}
		return "block:" + dimension.identifier().getNamespace() + ":" + dimension.identifier().getPath() + ":"
				+ pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
	}

	/** Reverses {@link #toStorageKey()}; returns null for unknown/malformed keys. */
	public static ContainerSettingsKey fromStorageKey(String key) {
		if (key == null) {
			return null;
		}
		if (PLAYER_INVENTORY_FILE.equals(key)) {
			return playerInventory();
		}
		if (!key.startsWith("block:")) {
			return null;
		}
		String[] parts = key.split(":", 4);
		if (parts.length != 4) {
			return null;
		}
		String[] coords = parts[3].split("_");
		if (coords.length != 3) {
			return null;
		}
		try {
			BlockPos pos = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
			return block(ResourceKey.create(Registries.DIMENSION, Identifier.tryParse(parts[1] + ":" + parts[2])), pos);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ContainerSettingsKey other)) {
			return false;
		}
		return playerInventory == other.playerInventory
				&& java.util.Objects.equals(dimension, other.dimension)
				&& java.util.Objects.equals(pos, other.pos);
	}

	@Override
	public int hashCode() {
		int result = java.util.Objects.hashCode(dimension);
		result = 31 * result + java.util.Objects.hashCode(pos);
		result = 31 * result + (playerInventory ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		if (playerInventory) {
			return "ContainerSettingsKey{playerInventory}";
		}
		return "ContainerSettingsKey{" + dimension.identifier() + "@" + pos + "}";
	}
}