package com.sighs.sophisticatedsorter.settings;

import net.minecraft.world.item.ItemStack;

/**
 * The minimal modifiable slot-stack view the container settings need over a real container
 * (a block entity's inventory or the player inventory). NeoForge's port of the same files uses
 * {@code IItemHandlerModifiable} here - the fabric ecosystem has no such interface (the fabric port
 * of Sophisticated Core builds on Porting Lib's transfer storage), so the settings code depends on
 * this small seam instead. The write-through inventory handler in {@link ContainerInventoryHandler}
 * only ever needs reads and writes of whole stacks per slot, plus a per-slot limit.
 */
public interface ContainerInventoryHandle {
	int getSlots();

	ItemStack getStackInSlot(int slot);

	void setStackInSlot(int slot, ItemStack stack);

	int getSlotLimit(int slot);
}
