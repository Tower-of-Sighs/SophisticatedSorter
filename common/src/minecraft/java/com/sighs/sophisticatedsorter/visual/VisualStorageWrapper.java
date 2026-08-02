package com.sighs.sophisticatedsorter.visual;

import com.sighs.sophisticatedsorter.common.AbstractVisualStorageWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

/** Core type bridge; the inert implementation is shared by common. */
public class VisualStorageWrapper extends AbstractVisualStorageWrapper<
        ITrackedContentsItemHandler, InventoryHandler, ITrackedContentsItemHandler,
        SettingsHandler, UpgradeHandler, SortBy, Player, RenderInfo, Component>
        implements IStorageWrapper {
    protected VisualStorageWrapper() {
        super(VisualSettingsHandler::new);
    }
}
