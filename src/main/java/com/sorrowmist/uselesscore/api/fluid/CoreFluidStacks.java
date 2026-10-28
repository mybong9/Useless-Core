package com.sorrowmist.uselesscore.api.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;

public final class CoreFluidStacks {
    private CoreFluidStacks() {}
    public static FluidStack copyWithAmount(FluidStack stack, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative fluid amount");
        FluidStack copy = stack.copy();
        copy.setAmount(amount);
        return copy;
    }
    public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
        return first.getFluid() == second.getFluid() && FluidStack.areFluidStackTagsEqual(first, second);
    }
    public static CompoundTag save(FluidStack stack) { return stack.writeToNBT(new CompoundTag()); }
    public static CompoundTag save(FluidStack stack, net.minecraft.core.HolderLookup.Provider registries) {
        return save(stack);
    }
    public static FluidStack load(CompoundTag tag) { return FluidStack.loadFluidStackFromNBT(tag); }
    public static FluidStack parseOptional(net.minecraft.core.HolderLookup.Provider registries, CompoundTag tag) {
        return load(tag);
    }
}
