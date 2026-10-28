package com.sorrowmist.uselesscore.api.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/** NBT-backed 1.20.1 equivalent of a component-sensitive fluid-stack ingredient. */
public final class DataComponentFluidIngredient extends FluidIngredient {
    private final FluidStack stack;
    private final boolean strict;

    private DataComponentFluidIngredient(boolean strict, FluidStack stack) {
        this.strict = strict;
        this.stack = stack.copy();
    }

    public static DataComponentFluidIngredient of(boolean strict, FluidStack stack) {
        return new DataComponentFluidIngredient(strict, stack);
    }

    public boolean isStrict() { return strict; }

    public FluidStack stack() {
        return stack.copy();
    }

    @Override
    public boolean test(FluidStack candidate) {
        if (candidate == null || candidate.isEmpty() || stack.getFluid() != candidate.getFluid()) return false;
        return strict ? FluidStack.areFluidStackTagsEqual(stack, candidate)
                : net.minecraft.nbt.NbtUtils.compareNbt(stack.getTag(), candidate.getTag(), true);
    }

    @Override
    public List<FluidStack> getStacksList() {
        return stack.isEmpty() ? List.of() : List.of(stack.copy());
    }
}
