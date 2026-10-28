package com.sorrowmist.uselesscore.api.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public final class CompoundFluidIngredient extends FluidIngredient {
    public static CompoundFluidIngredient of(List<? extends FluidIngredient> children) {
        return new CompoundFluidIngredient(children);
    }
    private final List<FluidIngredient> children;

    public CompoundFluidIngredient(List<? extends FluidIngredient> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public boolean test(FluidStack stack) {
        return children.stream().anyMatch(child -> child.test(stack));
    }

    @Override
    public List<FluidStack> getStacksList() {
        return children.stream().flatMap(child -> child.getStacksList().stream()).distinct().toList();
    }

    @Override
    public List<FluidIngredient> children() {
        return children;
    }
}
