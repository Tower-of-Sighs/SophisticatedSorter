package io.github.fabricators_of_create.porting_lib.core.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/**
 * Compile-time shim for Porting Lib's {@code INBTSerializable} interface.
 * <p>
 * The container-settings write-through inventory handler must subclass Sophisticated Core's
 * {@code InventoryHandler}, which on the Fabric port extends Porting Lib's transfer
 * {@code ItemStackHandler}; that class's implements-clause references
 * {@code io.github.fabricators_of_create.porting_lib.core.util.INBTSerializable}, so javac needs this
 * interface readable whenever a subclass is compiled. The target's build script only puts the
 * Porting Lib <i>transfer</i> module on the compile classpath (the <i>core</i> module - which owns
 * this type - is declared runtime-only and is extracted from the Sophisticated Core jar at runtime),
 * and the build scripts are not to be touched by this port, so this source copy exists solely to let
 * javac resolve the ancestor interface. It mirrors the real interface exactly (same package, name,
 * generic bound and method descriptors) and is therefore binary-identical to the class the runtime
 * provides through the Sophisticated Core jar's nested Porting Lib libraries; our own code never
 * calls through it. Do not extend this type or add members to it.
 */
public interface INBTSerializable<T extends Tag> {
	T serializeNBT(HolderLookup.Provider registries);

	void deserializeNBT(HolderLookup.Provider registries, T nbt);
}
