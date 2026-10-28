package com.sorrowmist.uselesscore.api.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

/** Uses Forge 1.20.1 ingredient serializers for JSON, NBT codecs and network transport. */
public final class CoreIngredientCodecs {
    public static final Codec<Ingredient> INGREDIENT = Codec.PASSTHROUGH.comapFlatMap(
            CoreIngredientCodecs::decode, ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson()));
    public static final Codec<Ingredient> NONEMPTY = INGREDIENT.flatXmap(
            CoreIngredientCodecs::nonempty, CoreIngredientCodecs::nonempty);
    public static final CoreStreamCodec<FriendlyByteBuf, Ingredient> STREAM = CoreStreamCodec.of(
            (buffer, ingredient) -> ingredient.toNetwork(buffer), Ingredient::fromNetwork);

    private CoreIngredientCodecs() {}

    public static boolean isCustom(Ingredient ingredient) { return !ingredient.isVanilla(); }

    public static TagKey<Item> tagValue(Ingredient.TagValue value) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(value.serialize().get("tag").getAsString()));
    }

    public static Object custom(Ingredient ingredient) {
        CoreCustomIngredient wrapped = CoreCustomIngredient.unwrap(ingredient);
        return wrapped != null ? wrapped : ingredient.isVanilla() ? null : ingredient;
    }

    /** Recover vanilla item/tag values through their public JSON representation. */
    public static Ingredient.Value[] values(Ingredient ingredient) {
        if (!ingredient.isVanilla()) throw new IllegalArgumentException("Custom ingredient has no vanilla values");
        JsonElement json = ingredient.toJson();
        java.util.List<Ingredient.Value> values = new java.util.ArrayList<>();
        Iterable<JsonElement> entries = json.isJsonArray() ? json.getAsJsonArray() : java.util.List.of(json);
        for (JsonElement entry : entries) {
            var object = entry.getAsJsonObject();
            if (object.has("tag")) {
                values.add(new Ingredient.TagValue(TagKey.create(Registries.ITEM,
                        new ResourceLocation(object.get("tag").getAsString()))));
            } else if (object.has("item")) {
                var id = new ResourceLocation(object.get("item").getAsString());
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown ingredient item: " + id));
                values.add(new Ingredient.ItemValue(new net.minecraft.world.item.ItemStack(item)));
            } else throw new IllegalArgumentException("Unknown vanilla ingredient: " + entry);
        }
        return values.toArray(Ingredient.Value[]::new);
    }

    private static DataResult<Ingredient> decode(Dynamic<?> input) {
        try {
            return DataResult.success(Ingredient.fromJson(input.convert(JsonOps.INSTANCE).getValue(), true));
        } catch (RuntimeException exception) {
            return DataResult.error(() -> "Invalid ingredient: " + exception.getMessage());
        }
    }

    private static DataResult<Ingredient> nonempty(Ingredient ingredient) {
        return ingredient.isEmpty() ? DataResult.error(() -> "Ingredient must not be empty")
                : DataResult.success(ingredient);
    }

    public static Optional<TagKey<Item>> directTag(Ingredient ingredient) {
        if (ingredient == null || !ingredient.isVanilla()) return Optional.empty();
        JsonElement json = ingredient.toJson();
        if (!json.isJsonObject() || !json.getAsJsonObject().has("tag")) return Optional.empty();
        ResourceLocation id = ResourceLocation.tryParse(json.getAsJsonObject().get("tag").getAsString());
        return id == null ? Optional.empty() : Optional.of(TagKey.create(Registries.ITEM, id));
    }
}
