package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.settings.ContainerMemorySlotGuard;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import java.util.OptionalInt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps {@link ContainerSettingsTracker} in sync with the player's container menus. NeoForge
 * exposes container open/close events; Fabric 1.20.1 has none, so this target tracks through the
 * vanilla server-side lifecycle methods instead:
 * <ul>
 * <li>{@code ServerPlayer.openMenu} RETURN - the universal server-side menu-open path (vanilla
 * chests, hoppers, this mod's settings swap, ...), the counterpart of
 * {@code PlayerContainerEvent.Open}.</li>
 * <li>{@code ServerPlayer.doCloseContainer} HEAD - the universal close funnel of this version: the
 * client close packet ({@code ServerboundContainerClosePacket}), the vanilla {@code closeContainer}
 * and the Fabric screen-handler soft close during menu swaps all pass through it. The counterpart of
 * {@code PlayerContainerEvent.Close}; also clears the memory-slot guard's stale armed state.</li>
 * <li>{@code ServerPlayer.changeDimension} HEAD - belt and braces, the counterpart of
 * {@code PlayerEvent.PlayerChangedDimensionEvent}.</li>
 * </ul>
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMenuTrackingMixin {
	@Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At("RETURN"))
	private void sophisticatedSorter$onPlayerOpenMenu(MenuProvider menuProvider, CallbackInfoReturnable<OptionalInt> cir) {
		ContainerSettingsTracker.onPlayerOpenMenu((ServerPlayer) (Object) this);
	}

	@Inject(method = "doCloseContainer", at = @At("HEAD"))
	private void sophisticatedSorter$onPlayerCloseContainer(CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		ContainerSettingsTracker.onPlayerCloseContainer(player);
		ContainerMemorySlotGuard.onContainerClose();
	}

	@Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"))
	private void sophisticatedSorter$onPlayerChangedDimension(ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
		ContainerSettingsTracker.onPlayerChangedDimension((ServerPlayer) (Object) this);
	}
}