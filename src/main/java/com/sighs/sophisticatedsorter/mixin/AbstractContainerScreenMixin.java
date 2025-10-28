package com.sighs.sophisticatedsorter.mixin;

import com.sighs.sophisticatedsorter.utils.CoreUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SortButtonsPosition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Shadow protected int leftPos;

    @Shadow protected int imageWidth;

    @Shadow protected int topPos;

    protected AbstractContainerScreenMixin(Component p_96550_) {
        super(p_96550_);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void q(CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) return;
        SortButtonsPosition sortButtonsPosition = Config.CLIENT.sortButtonsPosition.get();
        if (sortButtonsPosition != SortButtonsPosition.HIDDEN) {
            int x = leftPos + imageWidth - 31;
            int y = topPos + 4;
            var sortButton = new Button(new Position(x, y), ButtonDefinitions.SORT, (button) -> {
                if (button == 0) {
                    CoreUtils.serverSort();
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("Sorted"), true);
                }
            });
            this.addRenderableWidget(sortButton);
            var toggleButton = new ToggleButton(new Position(x + 12, y), ButtonDefinitions.SORT_BY, (button) -> {
                if (button == 0) {
                    CoreUtils.toggleSortBy();
                }
            }, CoreUtils::getSortBy);
            this.addRenderableWidget(toggleButton);
        }
    }
}
