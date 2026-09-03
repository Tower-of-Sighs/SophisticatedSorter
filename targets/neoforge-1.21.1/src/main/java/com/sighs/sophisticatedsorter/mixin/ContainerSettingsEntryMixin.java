package com.sighs.sophisticatedsorter.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sighs.sophisticatedsorter.client.settings.ContainerSettingsTab;
import com.sighs.sophisticatedsorter.client.settings.ContainerSlotHighlighter;
import com.sighs.sophisticatedsorter.settings.ContainerSettingsKey;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NeoForge-1.21.1 only: adds the settings entry - a Sophisticated Core {@link net.p3pp3rf1y.sophisticatedcore.client.gui.Tab}
 * with the settings gear, the same tab Core's own storage screens render - to qualifying vanilla
 * container screens and to the player-inventory main screen. Also renders the per-slot highlight
 * stripes (no-sort / item-display colors) of the block-entity container on the normal container
 * screen, so the highlights the settings panel configures stay visible outside it, and the memory
 * slot ghosts: an empty slot with a memorized item shows that item under Core's translucent ghost
 * overlay, exactly like Core's storage screens render empty memorized slots.
 * <p>
 * This target shares {@code common}'s {@code AbstractContainerScreenMixin} with the other targets,
 * so the entry cannot live there; this target-local mixin reuses the shared screen's shadow fields
 * and widget list, and only ever runs on this target's client.
 * <p>
 * Screens that have their own settings entry (Core storage screens, including the sorter's own
 * container-settings screen) or the creative inventory are skipped. The tab sits flush against the
 * right edge of the container background ({@code leftPos + imageWidth}), exactly where Core places
 * its settings/upgrade tab column.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerSettingsEntryMixin extends Screen {
	@Shadow
	protected int leftPos;
	@Shadow
	protected int imageWidth;
	@Shadow
	protected int topPos;

	@Unique
	private ContainerSettingsTab sophisticatedSorter$settingsTab;
	/** Settings of the currently tracked container, rebuilt when the server-pushed key changes. */
	@Unique
	private SettingsHandler sophisticatedSorter$highlightSettings;
	/** The tracked key the cached settings handler was built for. */
	@Unique
	private ContainerSettingsKey sophisticatedSorter$highlightKey;
	/** Memory category of the currently tracked container (same lifecycle as the settings above). */
	@Unique
	private MemorySettingsCategory sophisticatedSorter$memoryCategory;
	protected ContainerSettingsEntryMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void sophisticatedSorter$addSettingsTab(CallbackInfo ci) {
		AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) (Object) this;
		this.sophisticatedSorter$highlightSettings = null;
		this.sophisticatedSorter$highlightKey = null;
		this.sophisticatedSorter$memoryCategory = null;
		if (this.sophisticatedSorter$settingsTab != null) {
			this.removeWidget(this.sophisticatedSorter$settingsTab);
			this.sophisticatedSorter$settingsTab = null;
		}
		if (!isEligible(containerScreen)) {
			return;
		}
		ContainerSettingsKey explicitKey = containerScreen instanceof InventoryScreen
				? ContainerSettingsKey.playerInventory()
				: null;
		this.sophisticatedSorter$settingsTab = new ContainerSettingsTab(
				new Position(leftPos + imageWidth, topPos + 4), explicitKey);
		this.addRenderableWidget(this.sophisticatedSorter$settingsTab);
	}

	@Inject(method = "renderTooltip", at = @At("HEAD"))
	private void sophisticatedSorter$renderSettingsTabTooltip(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
		if (this.sophisticatedSorter$settingsTab != null) {
			this.sophisticatedSorter$settingsTab.renderTooltip(this, guiGraphics, x, y);
		}
	}

	/**
	 * Draws the per-slot highlight stripes (no-sort / item-display colors) and the memory-slot ghosts
	 * over the storage slots of the block-entity container this vanilla screen shows, mirroring Core's
	 * storage screens. The tracked key (server-pushed) may arrive a tick after the screen opens, so the
	 * handler is rebuilt whenever the tracked key changes.
	 */
	@Inject(method = "renderSlot", at = @At("RETURN"))
	private void sophisticatedSorter$renderSlotHighlights(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
		// Core's storage/settings screens draw their own highlights and have their own gear entry;
		// only decorate vanilla container screens (creative excluded via eligibility of the entry).
		Object self = this;
		if (self instanceof StorageScreenBase || self instanceof SettingsScreen || self instanceof CreativeModeInventoryScreen
				|| slot.container instanceof Inventory) {
			return;
		}
		ContainerSettingsKey trackedKey = com.sighs.sophisticatedsorter.client.settings.ClientTrackedContainer.getCurrentKey();
		if (trackedKey == null || trackedKey.isPlayerInventory()) {
			this.sophisticatedSorter$highlightSettings = null;
			this.sophisticatedSorter$highlightKey = null;
			this.sophisticatedSorter$memoryCategory = null;
			return;
		}
		if (this.sophisticatedSorter$highlightKey == null || !this.sophisticatedSorter$highlightKey.equals(trackedKey)) {
			this.sophisticatedSorter$highlightSettings = ContainerSlotHighlighter.settingsForTrackedContainer();
			this.sophisticatedSorter$highlightKey = trackedKey;
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

	@Unique
	private static boolean isEligible(AbstractContainerScreen<?> containerScreen) {
		if (containerScreen instanceof StorageScreenBase || containerScreen instanceof SettingsScreen
				|| containerScreen instanceof CreativeModeInventoryScreen) {
			return false;
		}
		// The player inventory main screen (InventoryMenu) is a supported settings target, so only
		// other screens whose menu is the player inventory (e.g. creative) are excluded above.
		return !(containerScreen.getMenu() instanceof InventoryMenu)
				|| containerScreen instanceof InventoryScreen;
	}
}
