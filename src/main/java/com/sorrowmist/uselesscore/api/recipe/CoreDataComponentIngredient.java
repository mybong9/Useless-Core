package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

/** Component-sensitive item matching backed by Forge's native NBT ingredients. */
public final class CoreDataComponentIngredient {
    private CoreDataComponentIngredient() {}

    public static Ingredient of(boolean strict, ItemStack stack) {
        if (stack.isEmpty()) return Ingredient.EMPTY;
        if (strict) return StrictNBTIngredient.of(stack.copyWithCount(1));
        return PartialNBTIngredient.of(stack.getItem(), stack.getTag() == null
                ? new net.minecraft.nbt.CompoundTag() : stack.getTag().copy());
    }
}
