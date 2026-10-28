package com.sorrowmist.uselesscore.api.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/** Registry-free extension point for mod-defined alloy-furnace catalysts. */
public final class CoreCatalystRegistry {
    private static final Map<ResourceLocation, CoreCatalystType> TYPES = new LinkedHashMap<>();

    private CoreCatalystRegistry() {
    }

    public static synchronized CoreCatalystType register(
            ResourceLocation id, Supplier<? extends Item> item, int tier, String displayName,
            IntSupplier normalRecipeParallel, IntUnaryOperator processTime,
            boolean infiniteParallel, boolean tiered) {
        Objects.requireNonNull(id, "id");
        if (TYPES.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate catalyst id: " + id);
        }
        CoreCatalystType type = new CoreCatalystType(id, item, tier, displayName,
                normalRecipeParallel, processTime, infiniteParallel, tiered);
        TYPES.put(id, type);
        return type;
    }

    public static synchronized List<CoreCatalystType> all() {
        return Collections.unmodifiableList(new ArrayList<>(TYPES.values()));
    }

    public static CoreCatalystType none() {
        return CoreCatalystType.NONE;
    }

    public static synchronized @Nullable CoreCatalystType byTier(int tier) {
        return TYPES.values().stream().filter(type -> type.isUselessIngotTier() && type.getTier() == tier)
                .findFirst().orElse(null);
    }

    public static CoreCatalystType fromStack(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return CoreCatalystType.NONE;
        for (CoreCatalystType type : all()) {
            if (type.matches(stack)) return type;
        }
        return CoreCatalystType.NONE;
    }
}
