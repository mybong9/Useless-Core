package com.sorrowmist.uselesscore.api.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Recipe stacks accept 1.21's lowercase count and the original 1.20 NBT codec shape. */
public final class CoreItemStackCodecs {
    private static final Codec<ItemStack> RECIPE_STACK = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemStack::getItem),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(stack ->
                    java.util.Optional.ofNullable(stack.getTag()).map(CompoundTag::copy))
    ).apply(instance, (item, count, tag) -> {
        ItemStack stack = new ItemStack(item, count);
        tag.ifPresent(value -> stack.setTag(value.copy()));
        return stack;
    }));
    public static final Codec<ItemStack> STACK = Codec.either(ItemStack.CODEC, RECIPE_STACK)
            .xmap(value -> value.map(stack -> stack, stack -> stack), Either::right);
    private CoreItemStackCodecs() {}
}
