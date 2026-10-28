package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

public final class CorePotionStacks {
    private CorePotionStacks() {}
    public static ItemStack createItemStack(Item item, Potion potion) {
        return PotionUtils.setPotion(new ItemStack(item), potion);
    }
    public static ItemStack createItemStack(Item item, Holder<Potion> potion) {
        return createItemStack(item, potion.value());
    }
}
