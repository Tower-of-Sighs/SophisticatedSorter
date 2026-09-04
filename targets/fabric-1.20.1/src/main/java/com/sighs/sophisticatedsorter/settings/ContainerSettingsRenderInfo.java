package com.sighs.sophisticatedsorter.settings;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;

/**
 * {@link RenderInfo} that keeps its serialized form nowhere but in memory. Core only uses the
 * render info as a transport for the item-display render data of the storage's own slots; for
 * arbitrary containers nothing is rendered in the world, so both persistence callbacks are no-ops.
 * The empty tag returned by {@link #getRenderInfoTag()} is never mutated because
 * {@link #refreshItemDisplayRenderInfo} is only ever invoked from the settings screen on a client
 * (in-world rendering never runs for these targets).
 */
class ContainerSettingsRenderInfo extends RenderInfo {
	ContainerSettingsRenderInfo() {
		super(() -> () -> {});
	}

	@Override
	protected void serializeRenderInfo(CompoundTag renderInfo) {
	}

	@Override
	protected Optional<CompoundTag> getRenderInfoTag() {
		return Optional.empty();
	}
}