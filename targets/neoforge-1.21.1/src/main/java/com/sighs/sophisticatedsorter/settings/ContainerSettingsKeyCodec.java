package com.sighs.sophisticatedsorter.settings;

import com.sighs.sophisticatedsorter.SophisticatedSorter;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Codec helpers that wrap the {@link FriendlyByteBuf}-based key/menu wiring. Kept as a plain
 * utility so the payload classes and the menu registration stay thin.
 */
public final class ContainerSettingsKeyCodec {
	private ContainerSettingsKeyCodec() {
	}

	/** Writes the key; when null a type byte of {@code -1} is written so {@link #readNullable} can tell the two apart. */
	public static void writeNullable(@Nullable ContainerSettingsKey key, FriendlyByteBuf buf) {
		if (key == null) {
			buf.writeByte(-1);
		} else {
			key.write(buf);
		}
	}

	/** Reads back a key written with {@link #writeNullable}; returns null for the {@code -1} marker. */
	@Nullable
	public static ContainerSettingsKey readNullable(FriendlyByteBuf buf) {
		// Peek at the type byte without consuming it.
		int type = buf.getByte(buf.readerIndex());
		if (type == -1) {
			buf.readByte();
			return null;
		}
		return ContainerSettingsKey.fromBuffer(buf);
	}
}
