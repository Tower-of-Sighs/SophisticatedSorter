package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.settings.ContainerMemorySlotGuard;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge-1.20.1: arms the {@link ContainerMemorySlotGuard} for the duration of the server-side
 * container click ({@code ServerGamePacketListenerImpl.handleContainerClick}). Every player-driven
 * insertion into a menu (click placement, shift-drag, quick-move, drag-split) runs inside this
 * method, and it is the only place the mod can learn the acting {@link ServerPlayer} -
 * {@code Slot.mayPlace} has no player reference, and the client runs the same menu methods for
 * prediction against shared single-player menu objects, so the guard must be armed only here, on
 * the server thread.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerContainerClickMixin {
	@Inject(method = "handleContainerClick", at = @At("HEAD"))
	private void sophisticatedSorter$armMemorySlotGuard(ServerboundContainerClickPacket packet, CallbackInfo ci) {
		ServerPlayer player = ((ServerGamePacketListenerImpl) (Object) this).player;
		if (player != null) {
			ContainerMemorySlotGuard.arm(player);
		}
	}

	@Inject(method = "handleContainerClick", at = @At("RETURN"))
	private void sophisticatedSorter$disarmMemorySlotGuard(ServerboundContainerClickPacket packet, CallbackInfo ci) {
		ContainerMemorySlotGuard.disarm();
	}
}