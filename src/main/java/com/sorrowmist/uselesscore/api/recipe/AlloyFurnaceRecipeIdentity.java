package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Stable identity shared by the alloy-furnace API and serialized pattern metadata. */
public record AlloyFurnaceRecipeIdentity(ResourceLocation recipeId, String fingerprint) {
    public AlloyFurnaceRecipeIdentity {
        Objects.requireNonNull(recipeId, "recipeId");
        Objects.requireNonNull(fingerprint, "fingerprint");
        if (fingerprint.isBlank()) {
            throw new IllegalArgumentException("Recipe fingerprint cannot be blank");
        }
    }
}
