package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/** Bridges shared recipe implementations to the native 1.20.1 recipe contract. */
public interface CoreRecipe<C extends Container> extends Recipe<C> {
    ItemStack assemble(C input, HolderLookup.Provider registries);
    ItemStack getResultItem(HolderLookup.Provider registries);

    @Override
    default ItemStack assemble(C input, RegistryAccess registries) {
        return assemble(input, (HolderLookup.Provider) registries);
    }

    @Override
    default ItemStack getResultItem(RegistryAccess registries) {
        return getResultItem((HolderLookup.Provider) registries);
    }

    @Override
    default ResourceLocation getId() {
        return CoreRecipeIdentities.id(this);
    }
}
