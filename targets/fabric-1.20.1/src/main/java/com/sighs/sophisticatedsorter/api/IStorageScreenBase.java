package com.sighs.sophisticatedsorter.api;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public interface IStorageScreenBase {
    Predicate<ItemStack> getVisualStackFilter(String searchPhrase);
}
