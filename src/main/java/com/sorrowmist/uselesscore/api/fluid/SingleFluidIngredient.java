package com.sorrowmist.uselesscore.api.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public final class SingleFluidIngredient extends FluidIngredient {
    private final FluidStack stack;

    public SingleFluidIngredient(FluidStack stack) {
        this.stack = stack.copy();
    }

    public FluidStack stack() {
        return stack.copy();
    }

    @Override
    public boolean test(FluidStack candidate) {
        return candidate != null && !candidate.isEmpty() && stack.getFluid() == candidate.getFluid();
    }

    @Override
    public List<FluidStack> getStacksList() {
        return stack.isEmpty() ? List.of() : List.of(stack.copy());
    }
}
