package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Objects;

/**
 * Backport of the recipe identity/value pair used by newer Minecraft recipe APIs.
 * Forge 1.20.1 stores recipe ids on the recipe itself, so ordinary holders are
 * constructed directly from the recipe and generated holders may supply an id.
 */
public record CoreRecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
    public CoreRecipeHolder {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(value, "value");
        if (value instanceof CoreRecipe<?>) CoreRecipeIdentities.bind(value, id);
    }

    public CoreRecipeHolder(T value) {
        this(Objects.requireNonNull(value, "value").getId(), value);
    }
}
