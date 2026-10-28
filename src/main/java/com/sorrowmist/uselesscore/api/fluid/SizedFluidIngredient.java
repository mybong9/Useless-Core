package com.sorrowmist.uselesscore.api.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sorrowmist.uselesscore.api.network.codec.CoreByteBufCodecs;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public record SizedFluidIngredient(FluidIngredient ingredient, int amount) {
    public static final Codec<SizedFluidIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidIngredient.CODEC.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
            Codec.INT.fieldOf("amount").forGetter(SizedFluidIngredient::amount)
    ).apply(instance, SizedFluidIngredient::new));

    public static final CoreStreamCodec<FriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC =
            CoreStreamCodec.composite(
                    FluidIngredient.STREAM_CODEC, SizedFluidIngredient::ingredient,
                    CoreByteBufCodecs.VAR_INT, SizedFluidIngredient::amount,
                    SizedFluidIngredient::new);

    public SizedFluidIngredient {
        Objects.requireNonNull(ingredient, "ingredient");
        if (amount <= 0) throw new IllegalArgumentException("Fluid ingredient amount must be positive");
    }

    public static SizedFluidIngredient of(FluidStack stack) {
        return new SizedFluidIngredient(FluidIngredient.single(stack), stack.getAmount());
    }

    public FluidStack[] getFluids() {
        return ingredient.getStacksList().stream()
                .map(stack -> CoreFluidStacks.copyWithAmount(stack, amount)).toArray(FluidStack[]::new);
    }
}
