package com.sighs.sophisticatedsorter.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import com.sighs.sophisticatedsorter.client.settings.ClientTrackedContainer;
import com.sighs.sophisticatedsorter.client.settings.ContainerSlotHighlighter;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge-1.20.1: renders the per-slot highlights of the currently open block-entity container on
 * vanilla container screens - the no-sort / item-display color stripes and the memory-slot ghost
 * (an empty memorized slot shows its remembered item under Core's translucent overlay), exactly like
 * Core's own storage screens. The settings entry itself lives in the shared top-right button group
 * (see the sorter controls mixin); this mixin only decorates the slots.
 * <p>
 * This target shares {@code common}'s {@code AbstractContainerScreenMixin} with the other targets,
 * so the decoration cannot live there; this target-local mixin only ever runs on this target's
 * client. The current container key is the server-pushed one from {@link ClientTrackedContainer}.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerSettingsEntryMixin extends Screen {
	/** Settings of the currently tracked container, rebuilt when the server-pushed key changes. */
	@Unique
	private SettingsHandler sophisticatedSorter$highlightSettings;
	/** The tracked key the cached settings handler was built for. */
	@Unique
	private ContainerSettingsKey sophisticatedSorter$highlightKey;
	/** Memory category of the currently tracked container (same lifecycle as the settings above). */
	@Unique
	private MemorySettingsCategory sophisticatedSorter$memoryCategory;
	/** The exact contents tag the settings handler was built from; rebuilt when the mirror replaces it. */
	@Unique
	private net.minecraft.nbt.CompoundTag sophisticatedSorter$highlightContents;

	protected ContainerSettingsEntryMixin(Component title) {
		super(title);
	}

	/**
	 * Draws the per-slot highlight stripes (no-sort / item-display colors) and the memory-slot ghosts
	 * over the storage slots of the block-entity container this vanilla screen shows, mirroring Core's
	 * storage screens. The tracked key and its contents (server-pushed) may arrive a tick after the
	 * screen opens, so the handler is rebuilt whenever the tracked key or the mirror's contents change.
	 */
	@Inject(method = "renderSlot", at = @At("RETURN"))
	private void sophisticatedSorter$renderSlotHighlights(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
		// Core's storage/settings screens draw their own highlights and have their own gear entry;
		// only decorate vanilla container screens. Player-inventory slots are never decorated.
		Object self = this;
		if (self instanceof StorageScreenBase || self instanceof SettingsScreen || self instanceof CreativeModeInventoryScreen
				|| slot.container instanceof Inventory) {
			return;
		}
		ContainerSettingsKey trackedKey = ClientTrackedContainer.getCurrentKey();
		if (trackedKey == null || trackedKey.isPlayerInventory()) {
			this.sophisticatedSorter$highlightSettings = null;
			this.sophisticatedSorter$highlightKey = null;
			this.sophisticatedSorter$highlightContents = null;
			this.sophisticatedSorter$memoryCategory = null;
			return;
		}
		net.minecraft.nbt.CompoundTag currentContents = ClientContainerSettingsCache.getOrCreateContents(trackedKey);
		if (this.sophisticatedSorter$highlightKey == null || !this.sophisticatedSorter$highlightKey.equals(trackedKey)
				|| this.sophisticatedSorter$highlightContents != currentContents) {
			this.sophisticatedSorter$highlightSettings = ContainerSlotHighlighter.settingsForContents(currentContents);
			this.sophisticatedSorter$highlightKey = trackedKey;
			this.sophisticatedSorter$highlightContents = currentContents;
			this.sophisticatedSorter$memoryCategory = this.sophisticatedSorter$highlightSettings == null
					? null
					: this.sophisticatedSorter$highlightSettings.getTypeCategory(MemorySettingsCategory.class);
		}
		if (this.sophisticatedSorter$highlightSettings == null) {
			return;
		}
		List<Integer> colors = ContainerSlotHighlighter.overlayColors(this.sophisticatedSorter$highlightSettings, slot.index);
		if (!colors.isEmpty()) {
			int stripeHeight = 16 / colors.size();
			for (int i = 0; i < colors.size(); i++) {
				int yOffset = i * stripeHeight;
				int height = i == colors.size() - 1 ? 16 - yOffset : stripeHeight;
				int color = colors.get(i);
				RenderSystem.disableDepthTest();
				guiGraphics.fillGradient(slot.x, slot.y + yOffset, slot.x + 16, slot.y + yOffset + height, 0, color, color);
				RenderSystem.enableDepthTest();
			}
		}
		// Memory ghost: only on genuinely empty slots. The vanilla renderSlot has already drawn nothing
		// there, and a real item in the slot must never be hidden behind the memorized stack.
		if (!slot.getItem().isEmpty() || this.sophisticatedSorter$memoryCategory == null) {
			return;
		}
		Optional<ItemStack> memorized = ContainerSlotHighlighter.memorizedStack(this.sophisticatedSorter$memoryCategory, slot.index);
		if (memorized.isEmpty()) {
			return;
		}
		RenderSystem.enableDepthTest();
		guiGraphics.renderItem(memorized.get(), slot.x, slot.y);
		drawStackOverlay(guiGraphics, slot.x, slot.y);
	}

	/**
	 * The translucent ghost overlay Core draws over a memorized empty slot: a blit of
	 * {@link GuiHelper#GUI_CONTROLS} at UV (77, 0) with size 16x16, blending enabled and depth
	 * disabled (mirrors {@code StorageScreenBase.drawStackOverlay}).
	 */
	@Unique
	private void drawStackOverlay(GuiGraphics guiGraphics, int x, int y) {
		guiGraphics.pose().pushPose();
		RenderSystem.enableBlend();
		RenderSystem.disableDepthTest();
		guiGraphics.blit(GuiHelper.GUI_CONTROLS, x, y, 77, 0, 16, 16);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		guiGraphics.pose().popPose();
	}
}