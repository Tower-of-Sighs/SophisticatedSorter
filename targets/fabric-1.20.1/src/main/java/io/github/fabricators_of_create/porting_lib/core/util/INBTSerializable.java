package io.github.fabricators_of_create.porting_lib.core.util;

import net.minecraft.nbt.Tag;

/**
 * Compile-time bridge for {@code io.github.fabricators_of_create.porting_lib.core.util.INBTSerializable},
 * the interface Sophisticated Core's {@code InventoryHandler} hierarchy (Porting Lib's
 * {@code ItemStackHandler}) implements.
 * <p>
 * This target's frozen {@code build.gradle} puts the Porting Lib transfer module on the compile
 * classpath for the core's item-transfer APIs but keeps {@code porting_lib_core} - which owns this
 * interface - runtime-only, so extending or instantiating the core {@code InventoryHandler} (as the
 * container-settings stage must, mirroring the NeoForge reference) cannot resolve the interface at
 * compile time. This class is an exact source mirror of the real interface (same package, name,
 * generic bound and the two abstract method descriptors {@code serializeNBT()} returning the
 * generic's {@link Tag} bound and {@code deserializeNBT(Tag)}), and {@code ItemStackHandler}
 * overrides both. At runtime whichever copy is loaded first (Porting Lib's or this package's)
 * behaves identically, since the method set is the same and {@code ItemStackHandler} supplies its
 * own implementations.
 */
public interface INBTSerializable<T extends Tag> {
	T serializeNBT();

	void deserializeNBT(T nbt);
}