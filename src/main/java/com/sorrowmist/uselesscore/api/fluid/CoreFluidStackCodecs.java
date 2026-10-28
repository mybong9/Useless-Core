package com.sorrowmist.uselesscore.api.fluid;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;

/** Canonical recipe JSON plus the native Forge 1.20.1 FluidName/Amount NBT codec shape. */
public final class CoreFluidStackCodecs {
    private static final Codec<FluidStack> RECIPE_STACK = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidStack::getFluid),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("amount").forGetter(FluidStack::getAmount),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(stack ->
                    java.util.Optional.ofNullable(stack.getTag()).map(CompoundTag::copy))
    ).apply(instance, (fluid, amount, tag) -> {
        FluidStack stack = new FluidStack(fluid, amount);
        tag.ifPresent(value -> stack.setTag(value.copy()));
        return stack;
    }));
    public static final Codec<FluidStack> STACK = Codec.either(FluidStack.CODEC, RECIPE_STACK)
            .xmap(value -> value.map(stack -> stack, stack -> stack), Either::right);
    private CoreFluidStackCodecs() {}
}
