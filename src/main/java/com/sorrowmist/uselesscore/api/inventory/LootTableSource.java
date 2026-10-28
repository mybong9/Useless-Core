package com.sorrowmist.uselesscore.api.inventory;

import net.minecraft.resources.ResourceLocation;

/** Exposes the native 1.20.1 container loot-table id before the first unpack. */
public interface LootTableSource {
    ResourceLocation uselessStretcher$getLootTable();
}
