package com.sighs.sophisticatedsorter.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/** Shared accessor for the field present in every supported menu mapping. */
@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {
    @Accessor("quickcraftSlots")
    Set<Slot> sophisticatedSorter$getQuickcraftSlots();
}
