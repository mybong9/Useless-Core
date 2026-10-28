package com.sorrowmist.uselesscore.api.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Forge custom ingredient serializers register during common setup, outside vanilla registries. */
public final class CoreIngredientRegistry {
    private final String namespace;
    private final Map<ResourceLocation, Supplier<? extends CoreIngredientType<?>>> entries = new LinkedHashMap<>();
    public CoreIngredientRegistry(String namespace) { this.namespace = namespace; }
    public <T extends CoreCustomIngredient> Supplier<CoreIngredientType<T>> register(
            String path, Supplier<CoreIngredientType<T>> factory) {
        ResourceLocation id = new ResourceLocation(namespace, path);
        Supplier<CoreIngredientType<T>> lazy = new Supplier<>() {
            private CoreIngredientType<T> value;
            @Override public synchronized CoreIngredientType<T> get() {
                if (value == null) { value = factory.get(); value.bind(id); }
                return value;
            }
        };
        if (entries.putIfAbsent(id, lazy) != null) throw new IllegalArgumentException("Duplicate ingredient " + id);
        return lazy;
    }
    public void register(IEventBus bus) {
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() ->
                entries.forEach((id, supplier) -> CraftingHelper.register(id, supplier.get()))));
    }
}
