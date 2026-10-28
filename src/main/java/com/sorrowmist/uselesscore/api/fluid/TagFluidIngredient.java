package com.sorrowmist.uselesscore.api.fluid;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

public final class TagFluidIngredient extends FluidIngredient {
    private final TagKey<Fluid> tag;

    public TagFluidIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    public TagKey<Fluid> tag() {
        return tag;
    }

    @Override
    public boolean test(FluidStack stack) {
        return stack != null && !stack.isEmpty() && stack.getFluid().builtInRegistryHolder().is(tag);
    }

    @Override
    public List<FluidStack> getStacksList() {
        return FluidIngredient.resolveTag(tag);
    }

    @Override
    public Optional<TagKey<Fluid>> tagKey() {
        return Optional.of(tag);
    }
}
