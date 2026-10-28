package com.sorrowmist.uselesscore.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/** Keeps holder-owned recipe ids available to native APIs without retaining recipes across reloads. */
public final class CoreRecipeIdentities {
    private static final ReferenceQueue<Recipe<?>> EXPIRED = new ReferenceQueue<>();
    private static final Map<Identity, ResourceLocation> IDS = new HashMap<>();

    private CoreRecipeIdentities() {}

    public static synchronized void bind(Recipe<?> recipe, ResourceLocation id) {
        purge();
        IDS.putIfAbsent(new Identity(recipe, EXPIRED), id);
    }

    public static synchronized ResourceLocation id(Recipe<?> recipe) {
        purge();
        ResourceLocation id = IDS.get(new Identity(recipe, null));
        if (id == null) throw new IllegalStateException("Bind a CoreRecipeHolder id before using this recipe");
        return id;
    }

    private static void purge() {
        Identity expired;
        while ((expired = (Identity) EXPIRED.poll()) != null) IDS.remove(expired);
    }

    private static final class Identity extends WeakReference<Recipe<?>> {
        private final int hash;
        private Identity(Recipe<?> recipe, ReferenceQueue<Recipe<?>> queue) {
            super(recipe, queue);
            hash = System.identityHashCode(recipe);
        }
        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) {
            return this == other || other instanceof Identity identity
                    && get() != null && get() == identity.get();
        }
    }
}
