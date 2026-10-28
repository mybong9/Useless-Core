package com.sorrowmist.uselesscore.api.recipe;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/** Immutable views of vanilla and Forge brewing registrations, including mod-added mixes. */
public final class CoreBrewingRecipes {
    public record PotionMix(Holder<Potion> input, Ingredient ingredient, Holder<Potion> output) {}
    public record ContainerMix(Item input, Ingredient ingredient, Item output) {}
    public record Snapshot(List<PotionMix> potionMixes, List<ContainerMix> containerMixes,
                           List<IBrewingRecipe> recipes) {
        public Snapshot {
            potionMixes = List.copyOf(potionMixes);
            containerMixes = List.copyOf(containerMixes);
            recipes = List.copyOf(recipes);
        }
        public List<IBrewingRecipe> getRecipes() { return recipes; }
        public boolean isIngredient(ItemStack stack) {
            return potionMixes.stream().anyMatch(mix -> mix.ingredient().test(stack))
                    || containerMixes.stream().anyMatch(mix -> mix.ingredient().test(stack))
                    || recipes.stream().anyMatch(recipe -> recipe.isIngredient(stack));
        }
        public boolean hasPotionMix(ItemStack input, ItemStack reagent) {
            Potion potion = PotionUtils.getPotion(input);
            return potionMixes.stream().anyMatch(mix -> mix.input().value() == potion && mix.ingredient().test(reagent));
        }
        public boolean hasContainerMix(ItemStack input, ItemStack reagent) {
            return containerMixes.stream().anyMatch(mix -> input.is(mix.input()) && mix.ingredient().test(reagent));
        }
        public boolean hasMix(ItemStack input, ItemStack reagent) {
            return recipes.stream().anyMatch(recipe -> !recipe.getOutput(input, reagent).isEmpty());
        }
        public ItemStack mix(ItemStack reagent, ItemStack input) {
            for (IBrewingRecipe recipe : recipes) {
                ItemStack output = recipe.getOutput(input, reagent);
                if (!output.isEmpty()) return output.copy();
            }
            return ItemStack.EMPTY;
        }
    }
    private static volatile Snapshot cached;
    private CoreBrewingRecipes() {}

    public static synchronized Snapshot snapshot() {
        // Forge's helper remaps these stable 1.20.1 SRG field names in development and production.
        List<PotionBrewing.Mix<Potion>> potions = ObfuscationReflectionHelper.getPrivateValue(
                PotionBrewing.class, null, "f_43494_");
        List<PotionBrewing.Mix<Item>> containers = ObfuscationReflectionHelper.getPrivateValue(
                PotionBrewing.class, null, "f_43495_");
        Snapshot next = new Snapshot(
                List.copyOf(potions).stream().map(mix -> new PotionMix(mix.from, mix.ingredient, mix.to)).toList(),
                List.copyOf(containers).stream().map(mix -> new ContainerMix(mix.from.value(), mix.ingredient, mix.to.value())).toList(),
                List.copyOf(BrewingRecipeRegistry.getRecipes()));
        if (!next.equals(cached)) cached = next;
        return cached;
    }
}
