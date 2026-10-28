package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Recipe identity views backed by the native Forge 1.20.1 recipe manager. */
public final class CoreRecipes {
    private CoreRecipes() {}

    public static <T extends Recipe<?>> List<CoreRecipeHolder<T>> holders(Collection<? extends T> recipes) {
        List<CoreRecipeHolder<T>> result = new ArrayList<>(recipes.size());
        for (T recipe : recipes) result.add(new CoreRecipeHolder<>(recipe));
        return List.copyOf(result);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Recipe<?>> List<CoreRecipeHolder<T>> ofType(
            RecipeManager manager, RecipeType<? extends T> type) {
        return holders((Collection<? extends T>) manager.getAllRecipesFor((RecipeType) type));
    }

    public static List<CoreRecipeHolder<?>> all(RecipeManager manager) {
        List<CoreRecipeHolder<?>> result = new ArrayList<>();
        for (Recipe<?> recipe : manager.getRecipes()) result.add(new CoreRecipeHolder<>(recipe));
        return List.copyOf(result);
    }

    public static Optional<CoreRecipeHolder<?>> byKey(RecipeManager manager, ResourceLocation id) {
        return manager.byKey(id).map(CoreRecipeHolder::new);
    }

    public static <C extends Container, T extends Recipe<C>> Optional<CoreRecipeHolder<T>> find(
            RecipeManager manager, RecipeType<T> type, C input, Level level) {
        return manager.getRecipeFor(type, input, level).map(CoreRecipeHolder::new);
    }
}
