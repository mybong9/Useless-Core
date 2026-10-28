package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

/** Item post-processing for automated crafts in the native player-based 1.20.1 API. */
public final class CoreCraftingHooks {
    private CoreCraftingHooks() {}
    public static void onCraftedBySystem(ItemStack output, Level level) {
        if (!output.isEmpty() && level instanceof ServerLevel serverLevel) {
            output.onCraftedBy(level, FakePlayerFactory.getMinecraft(serverLevel), output.getCount());
        }
    }
}
