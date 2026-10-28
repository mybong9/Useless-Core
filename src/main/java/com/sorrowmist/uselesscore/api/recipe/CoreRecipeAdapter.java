package com.sorrowmist.uselesscore.api.recipe;

import appeng.api.stacks.AEKey;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import com.sorrowmist.uselesscore.api.recipe.CoreRecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Public contract for adapting a source recipe type to the alloy furnace.
 *
 * <p>The manager supplies merged inputs to the matching methods. Adapters should keep the
 * ingredient and component semantics represented by those maps instead of reducing them to a
 * representative stack.</p>
 *
 * @param <T> source recipe type
 */
public interface CoreRecipeAdapter<T extends Recipe<?>, R> {
    String DEFAULT_SOURCE_ID = "compatibility";

    /** Stable source identifier used when an adapter is registered without an explicit source. */
    default String sourceId() {
        return DEFAULT_SOURCE_ID;
    }

    /** Returns the source recipe class enumerated from the level's RecipeManager. */
    Class<T> getRecipeClass();

    /** Returns recipes synthesized from runtime data instead of RecipeManager entries. */
    default List<CoreRecipeHolder<T>> getGeneratedRecipes(Level level) {
        return List.of();
    }

    /** Converts one source recipe, or returns {@code null} when it is unsupported. */
    @Nullable
    default R convert(CoreRecipeHolder<T> holder, Level level) {
        List<R> recipes = convertAll(holder, level);
        return recipes.isEmpty() ? null : recipes.get(0);
    }

    /** Converts one source recipe into zero or more alloy-furnace recipes. */
    default List<R> convertAll(CoreRecipeHolder<T> holder, Level level) {
        R recipe = convert(holder, level);
        return recipe == null ? List.of() : List.of(recipe);
    }

    /**
     * Converts a source recipe using the concrete item stacks currently in the machine.
     * Implementations must not mutate {@code actualInputs}.
     */
    default List<R> convertAll(CoreRecipeHolder<T> holder, Level level,
                                                         List<ItemStack> actualInputs) {
        return convertAll(holder, level);
    }

    /** Returns the fixed mold used to pre-index this adapter, or {@code null} for a dynamic mold. */
    @Nullable
    ItemStack getMoldItem();

    /** Tests whether this adapter may handle the current mold. */
    default boolean matchesMold(@Nullable ItemStack mold) {
        ItemStack fixedMold = getMoldItem();
        if (fixedMold == null || fixedMold.isEmpty()) {
            return true;
        }
        return mold != null && !mold.isEmpty() && ItemStack.isSameItem(fixedMold, mold);
    }

    /** Legacy four-argument lookup for adapters without AEKey inputs. */
    @Nullable
    default CoreRecipeHolder<T> findMatchingRecipe(Level level, Map<Ingredient, Long> mergedInputs,
                                                Map<FluidStack, Long> mergedFluids,
                                                @Nullable ItemStack mold) {
        return null;
    }

    /** Returns all matches for the legacy four-argument lookup. */
    default List<CoreRecipeHolder<T>> findMatchingRecipes(Level level, Map<Ingredient, Long> mergedInputs,
                                                       Map<FluidStack, Long> mergedFluids,
                                                       @Nullable ItemStack mold) {
        CoreRecipeHolder<T> holder = findMatchingRecipe(level, mergedInputs, mergedFluids, mold);
        return holder == null ? List.of() : List.of(holder);
    }

    /** Lookup including merged AEKey inputs. */
    @Nullable
    default CoreRecipeHolder<T> findMatchingRecipe(Level level, Map<Ingredient, Long> mergedInputs,
                                                Map<FluidStack, Long> mergedFluids,
                                                Map<AEKey, Long> mergedKeys,
                                                @Nullable ItemStack mold) {
        return findMatchingRecipe(level, mergedInputs, mergedFluids, mold);
    }

    /** Returns all source recipes matching the merged item, fluid and AEKey inputs. */
    default List<CoreRecipeHolder<T>> findMatchingRecipes(Level level, Map<Ingredient, Long> mergedInputs,
                                                       Map<FluidStack, Long> mergedFluids,
                                                       Map<AEKey, Long> mergedKeys,
                                                       @Nullable ItemStack mold) {
        List<CoreRecipeHolder<T>> matches = findMatchingRecipes(level, mergedInputs, mergedFluids, mold);
        if (!matches.isEmpty()) {
            return matches;
        }
        CoreRecipeHolder<T> holder = findMatchingRecipe(level, mergedInputs, mergedFluids, mergedKeys, mold);
        return holder == null ? List.of() : List.of(holder);
    }

    /**
     * Lookup using both merged inputs and the concrete item stacks. Dynamic adapters may override
     * this when source recipe selection depends on components or another property of a stack.
     */
    default List<CoreRecipeHolder<T>> findMatchingRecipes(Level level, Map<Ingredient, Long> mergedInputs,
                                                       Map<FluidStack, Long> mergedFluids,
                                                       Map<AEKey, Long> mergedKeys,
                                                       @Nullable ItemStack mold,
                                                       List<ItemStack> actualInputs) {
        return findMatchingRecipes(level, mergedInputs, mergedFluids, mergedKeys, mold);
    }
}
