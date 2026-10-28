package com.sorrowmist.uselesscore.api.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Codec-based recipe serialization implemented through Forge 1.20.1 loading callbacks. */
public interface CoreRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {
    MapCodec<T> codec();
    CoreStreamCodec<FriendlyByteBuf, T> streamCodec();

    @Override
    default T fromJson(ResourceLocation id, JsonObject json) {
        JsonObject document = json.deepCopy();
        document.addProperty("id", id.toString());
        T recipe = codec().codec().parse(JsonOps.INSTANCE, document).getOrThrow(false,
                message -> { throw new JsonParseException("Recipe " + id + ": " + message); });
        if (recipe instanceof CoreRecipe<?>) CoreRecipeIdentities.bind(recipe, id);
        return recipe;
    }

    @Override
    default T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        T recipe = streamCodec().decode(buffer);
        if (recipe instanceof CoreRecipe<?>) CoreRecipeIdentities.bind(recipe, id);
        return recipe;
    }

    @Override
    default void toNetwork(FriendlyByteBuf buffer, T recipe) {
        streamCodec().encode(buffer, recipe);
    }
}
