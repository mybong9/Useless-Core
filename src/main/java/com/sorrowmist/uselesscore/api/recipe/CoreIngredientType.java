package com.sorrowmist.uselesscore.api.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public final class CoreIngredientType<T extends CoreCustomIngredient> implements IIngredientSerializer<CoreWrappedIngredient> {
    private final MapCodec<T> codec;
    private ResourceLocation id;
    public CoreIngredientType(MapCodec<T> codec) { this.codec = codec; }
    void bind(ResourceLocation id) {
        if (this.id != null) throw new IllegalStateException("Ingredient type already registered");
        this.id = id;
    }
    @SuppressWarnings("unchecked") JsonObject encode(CoreCustomIngredient value) {
        JsonObject json = codec.codec().encodeStart(JsonOps.INSTANCE, (T) value)
                .getOrThrow(false, message -> { throw new JsonParseException(message); }).getAsJsonObject();
        json.addProperty("type", java.util.Objects.requireNonNull(id, "Unregistered ingredient type").toString());
        return json;
    }
    @Override public CoreWrappedIngredient parse(JsonObject json) {
        T value = codec.codec().parse(JsonOps.INSTANCE, json)
                .getOrThrow(false, message -> { throw new JsonParseException(message); });
        return new CoreWrappedIngredient(value);
    }
    @Override public CoreWrappedIngredient parse(FriendlyByteBuf buffer) {
        return parse(com.google.gson.JsonParser.parseString(buffer.readUtf(1048576)).getAsJsonObject());
    }
    @Override public void write(FriendlyByteBuf buffer, CoreWrappedIngredient ingredient) {
        buffer.writeUtf(encode(ingredient.value()).toString(), 1048576);
    }
}
