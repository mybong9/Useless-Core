package com.sorrowmist.uselesscore.api.recipe;

import com.google.gson.JsonElement;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

final class CoreWrappedIngredient extends AbstractIngredient {
    private final CoreCustomIngredient value;
    CoreWrappedIngredient(CoreCustomIngredient value) { this.value = java.util.Objects.requireNonNull(value); }
    CoreCustomIngredient value() { return value; }
    @Override public boolean test(ItemStack stack) { return value.test(stack); }
    @Override public ItemStack[] getItems() { return value.getItems().map(ItemStack::copy).toArray(ItemStack[]::new); }
    @Override public boolean isSimple() { return value.isSimple(); }
    @Override public boolean isEmpty() { return value.getItems().findAny().isEmpty(); }
    @Override public IIngredientSerializer<?> getSerializer() { return value.getType(); }
    @Override public JsonElement toJson() { return value.getType().encode(value); }
}
