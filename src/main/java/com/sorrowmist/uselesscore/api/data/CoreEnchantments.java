package com.sorrowmist.uselesscore.api.data;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class CoreEnchantments {
    private CoreEnchantments() {}
    public static void setLevel(ItemStack stack, Enchantment enchantment, int level) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.getOrDefault(enchantment, 0) == level) return;
        if (level <= 0) enchantments.remove(enchantment);
        else enchantments.put(enchantment, level);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }
}
