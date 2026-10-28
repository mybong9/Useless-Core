package com.sorrowmist.uselesscore.api.recipe;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface CoreCustomIngredient {
    boolean test(ItemStack stack);
    Stream<ItemStack> getItems();
    boolean isSimple();
    CoreIngredientType<?> getType();
    default Ingredient toVanilla() { return wrap(this); }
    static Ingredient wrap(CoreCustomIngredient ingredient) { return new CoreWrappedIngredient(ingredient); }
    static CoreCustomIngredient unwrap(Ingredient ingredient) {
        return ingredient instanceof CoreWrappedIngredient wrapped ? wrapped.value() : null;
    }
}
