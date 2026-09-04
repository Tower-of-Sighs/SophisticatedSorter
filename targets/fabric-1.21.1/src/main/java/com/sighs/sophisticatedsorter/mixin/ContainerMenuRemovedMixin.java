package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.settings.ContainerMemorySlotGuard;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric-1.21.1 only: clears the container tracking when a menu is removed server-side, replacing
 * NeoForge's {@code PlayerContainerEvent.Close} listener (Fabric has no such event).
 * <p>
 * {@code AbstractContainerMenu.removed} fires on both the client (with the {@code LocalPlayer} and
 * the client-side menu copy) and the server (with the {@link ServerPlayer} and the real menu), so
 * the acting side is established by the player type. On the server it fires - just before
 * {@code containerMenu} is swapped back to the inventory menu - whenever the player closes a
 * container (ESC, death, dimension change, ...), which is exactly the moment the tracker must
 * forget the container so a stale gear click cannot act on it. The memory-slot guard's armed state
 * is cleared for the same reason.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMenuRemovedMixin {
	@Inject(method = "removed", at = @At("HEAD"))
	private void sophisticatedSorter$clearTrackingOnMenuRemoved(Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
			// Only the current menu is the container being closed; the default inventory menu is never
			// tracked and must not clear a freshly tracked container.
			if (menu == serverPlayer.containerMenu && menu != serverPlayer.inventoryMenu) {
				ContainerSettingsTracker.get().onContainerClosed(serverPlayer);
			}
			ContainerMemorySlotGuard.onContainerClosed();
		}
	}
}