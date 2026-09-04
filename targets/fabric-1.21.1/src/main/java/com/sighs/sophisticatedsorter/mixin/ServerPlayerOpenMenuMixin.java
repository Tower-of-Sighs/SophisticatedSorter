package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import java.util.OptionalInt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric-1.21.1 only: records which container each player just opened, replacing NeoForge's
 * {@code PlayerContainerEvent.Open} listener (Fabric has no such event).
 * <p>
 * The server is the only side that calls {@code ServerPlayer.openMenu}, so this injection runs
 * exactly where the container choice happens - for vanilla containers (chest, barrel, ...) the
 * vanilla body runs and the RETURN injection records the key derived from the resulting menu
 * (see {@link ContainerSettingsTracker#onContainerOpened}). The fabric extended-menu path (used by
 * Sophisticated Core's own settings screens) either cancels the callback before RETURN or opens a
 * menu whose container does not resolve to a supported block entity, so those menus are not
 * tracked - which is exactly what the container settings feature wants.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerOpenMenuMixin {
	/** The menu that was open before this call, to detect whether openMenu actually replaced it. */
	@Unique
	private net.minecraft.world.inventory.AbstractContainerMenu sophisticatedSorter$menuBeforeOpen;

	@Inject(method = "openMenu", at = @At("HEAD"))
	private void sophisticatedSorter$captureMenuBeforeOpen(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
		this.sophisticatedSorter$menuBeforeOpen = ((ServerPlayer) (Object) this).containerMenu;
	}

	@Inject(method = "openMenu", at = @At("RETURN"))
	private void sophisticatedSorter$trackOpenedMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		// Only record when the menu was actually replaced (createMenu returned a menu); on failure the
		// player's container menu is untouched and must not be re-tracked.
		if (player.containerMenu != this.sophisticatedSorter$menuBeforeOpen) {
			ContainerSettingsTracker.get().onContainerOpened(player);
		}
	}
}