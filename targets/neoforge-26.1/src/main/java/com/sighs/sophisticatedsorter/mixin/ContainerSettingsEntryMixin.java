package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.client.settings.ClientContainerSettingsCache;
import com.sighs.sophisticatedsorter.client.settings.ClientTrackedContainer;
import com.sighs.sophisticatedsorter.client.settings.ContainerSlotHighlighter;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
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
 * NeoForge-26.1 only: renders the per-slot highlights of the currently open block-entity container
 * on vanilla container screens - the no-sort / item-display color stripes and the memory-slot ghost
 * (an empty memorized slot shows its remembered item under Core's translucent overlay), exactly like
 * Core's own storage screens. The settings entry itself lives in the shared top-right button group
 * (see the sorter controls in {@code event.ScreenInit}); this mixin only decorates the slots.
 * <p>
 * This target shares {@code common}'s sorter mixins with the other targets, so the decoration cannot
 * live there; this target-local mixin only ever runs on this target's client. The current container
 * key is the server-pushed one from {@link ClientTrackedContainer}.
 * <p>
 * 26.1 differences from the 1.21.1 implementation: Minecraft renamed {@code renderSlot} to
 * {@code extractSlot} and {@code GuiGraphics} to {@code GuiGraphicsExtractor}; {@code RenderSystem}
 * depth/blend toggling was removed, so the stripes use the plain {@code fillGradient} overlay and
 * the memory ghost uses {@link GuiHelper#renderItemInGUI} plus the GUI_CONTROLS blit exactly as
 * Core's {@code StorageScreenBase.drawStackOverlay} does.
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
	@Inject(method = "extractSlot", at = @At("RETURN"))
	private void sophisticatedSorter$extractSlotHighlights(GuiGraphicsExtractor guiGraphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
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
			net.minecraft.core.HolderLookup.Provider registries = Minecraft.getInstance().level != null
					? Minecraft.getInstance().level.registryAccess()
					: null;
			if (registries == null) {
				return;
			}
			this.sophisticatedSorter$highlightSettings = ContainerSlotHighlighter.settingsForContents(currentContents, registries);
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
				guiGraphics.fillGradient(slot.x, slot.y + yOffset, slot.x + 16, slot.y + yOffset + height, color, color);
			}
		}
		// Memory ghost: only on genuinely empty slots. The vanilla extractSlot has already drawn nothing
		// there, and a real item in the slot must never be hidden behind the memorized stack.
		if (!slot.getItem().isEmpty() || this.sophisticatedSorter$memoryCategory == null) {
			return;
		}
		Optional<ItemStack> memorized = ContainerSlotHighlighter.memorizedStack(this.sophisticatedSorter$memoryCategory, slot.index);
		if (memorized.isEmpty()) {
			return;
		}
		GuiHelper.renderItemInGUI(guiGraphics, Minecraft.getInstance(), memorized.get(), slot.x, slot.y);
		drawStackOverlay(guiGraphics, slot.x, slot.y);
	}

	/**
	 * The translucent ghost overlay Core draws over a memorized empty slot: a blit of
	 * {@link GuiHelper#GUI_CONTROLS} at UV (77, 0) with size 16x16 (mirrors
	 * {@code StorageScreenBase.drawStackOverlay}).
	 */
	@Unique
	private void drawStackOverlay(GuiGraphicsExtractor guiGraphics, int x, int y) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiHelper.GUI_CONTROLS, x, y, 77.0F, 0.0F, 16, 16, 256, 256);
	}
}