package com.sorrowmist.uselesscore.api.registry;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Keeps registry context available to shared saved-data serializers on Forge 1.20.1. */
public abstract class CoreSavedData extends SavedData {
    private HolderLookup.Provider registries = RegistryAccess.EMPTY;
    public record Factory<T extends CoreSavedData>(Supplier<T> constructor,
            BiFunction<CompoundTag, HolderLookup.Provider, T> loader) {
        public Factory(Supplier<T> constructor, Function<CompoundTag, T> loader) {
            this(constructor, (tag, registries) -> loader.apply(tag));
        }
    }
    public abstract CompoundTag save(CompoundTag tag, HolderLookup.Provider registries);
    @Override public final CompoundTag save(CompoundTag tag) { return save(tag, registries); }
    public static <T extends CoreSavedData> T get(ServerLevel level, Factory<T> factory, String id) {
        T data = level.getDataStorage().computeIfAbsent(
                tag -> factory.loader().apply(tag, level.registryAccess()), factory.constructor(), id);
        ((CoreSavedData) data).registries = level.registryAccess();
        return data;
    }
}
