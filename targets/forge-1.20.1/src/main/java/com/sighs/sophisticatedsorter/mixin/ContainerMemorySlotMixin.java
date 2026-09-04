package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.settings.ContainerMemorySlotGuard;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge-1.20.1: enforces the per-container memory-slot filters on vanilla container screens.
 * <p>
 * Core's own storage screens already reject placements into memorized slots whose filter does not
 * match (their {@code InventoryHandler.isItemValid} ends with
 * {@code memory.matchesFilter(slot, stack)} and their storage slots delegate {@code mayPlace} to
 * that). A vanilla container menu (e.g. a chest) has plain {@link Slot}s whose container is the
 * block entity itself, so nothing enforces the memory filters this mod stores per container there.
 * This mixin closes that gap: whenever a player tries to place a stack into a memorized slot of a
 * vanilla container menu that the mod tracks, the placement is rejected exactly like core rejects
 * it.
 * <p>
 * The check is delegated to {@link ContainerMemorySlotGuard}, which is <b>server-side only</b>:
 * the guard is armed only while the server-side {@code ServerGamePacketListenerImpl.handleContainerClick}
 * is on the stack (see {@link ContainerMemorySlotGuard#arm}). On the client the same
 * {@code AbstractContainerMenu.clicked}/{@code quickMoveStack} calls run for prediction against
 * local {@code SimpleContainer} copies; {@code mayPlace} is consulted there too, but the tracker
 * key exists only on the server for a vanilla menu, and rejecting on the client would only
 * desynchronize the predicted state. The authoritative rejection happens on the server, where the
 * menu's storage slots reference the real block entity.
 */
@Mixin(Slot.class)
public abstract class ContainerMemorySlotMixin {
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void sophisticatedSorter$rejectMismatchedMemoryPlacement(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		Slot self = (Slot) (Object) this;
		Boolean rejected = ContainerMemorySlotGuard.rejectIfMemoryMismatch(self, stack);
		if (rejected != null) {
			cir.setReturnValue(rejected);
		}
	}
}