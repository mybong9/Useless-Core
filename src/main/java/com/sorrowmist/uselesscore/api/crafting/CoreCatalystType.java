package com.sorrowmist.uselesscore.api.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/** An extensible catalyst descriptor for long-running crafting machines. */
public final class CoreCatalystType {
    static final CoreCatalystType NONE = new CoreCatalystType(
            new ResourceLocation("useless_core", "none"), null, 0, "", () -> 1, value -> value,
            false, false);

    private final ResourceLocation id;
    private final Supplier<? extends Item> item;
    private final int tier;
    private final String displayName;
    private final IntSupplier normalRecipeParallel;
    private final IntUnaryOperator processTime;
    private final boolean infiniteParallel;
    private final boolean tiered;

    CoreCatalystType(ResourceLocation id, Supplier<? extends Item> item, int tier, String displayName,
                     IntSupplier normalRecipeParallel, IntUnaryOperator processTime,
                     boolean infiniteParallel, boolean tiered) {
        this.id = Objects.requireNonNull(id, "id");
        this.item = item;
        this.tier = tier;
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.normalRecipeParallel = Objects.requireNonNull(normalRecipeParallel, "normalRecipeParallel");
        this.processTime = Objects.requireNonNull(processTime, "processTime");
        this.infiniteParallel = infiniteParallel;
        this.tiered = tiered;
    }

    public ResourceLocation id() { return id; }
    public int getTier() { return tier; }
    public String getDisplayName() { return displayName; }
    public int getNormalRecipeParallel() { return normalRecipeParallel.getAsInt(); }
    public boolean isInfiniteParallel() { return infiniteParallel; }
    public boolean isUsefulIngot() { return infiniteParallel; }
    public boolean isValidCatalyst() { return item != null; }
    public boolean isUselessIngotTier() { return tiered; }

    public boolean matches(ItemStack stack) {
        return item != null && !stack.isEmpty() && stack.is(item.get());
    }

    public int calculateProcessTime(int baseTime) {
        return processTime.applyAsInt(baseTime);
    }
}
