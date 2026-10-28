package com.sorrowmist.uselesscore.api.component;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;

/** NBT-backed persistent entity component accessors for Forge/Minecraft 1.20.1. */
public final class CoreEntityComponents {
    private static final String ROOT_TAG = "UselessCoreComponents";

    private CoreEntityComponents() {}

    public static <T> T get(Entity entity, CoreDataComponentType<T> type) {
        return get(entity.getPersistentData(), type);
    }

    public static <T> void set(Entity entity, CoreDataComponentType<T> type, T value) {
        set(entity.getPersistentData(), type, value);
    }

    public static <T> T get(BlockEntity entity, CoreDataComponentType<T> type) {
        return get(entity.getPersistentData(), type);
    }

    public static <T> void set(BlockEntity entity, CoreDataComponentType<T> type, T value) {
        set(entity.getPersistentData(), type, value);
        entity.setChanged();
    }

    private static <T> T get(CompoundTag data, CoreDataComponentType<T> type) {
        CompoundTag components = data.getCompound(ROOT_TAG);
        if (!components.contains(type.id().toString())) return null;
        return type.persistentCodec().parse(NbtOps.INSTANCE, components.get(type.id().toString())).result().orElse(null);
    }

    private static <T> void set(CompoundTag data, CoreDataComponentType<T> type, T value) {
        CompoundTag components = data.getCompound(ROOT_TAG);
        if (value == null) components.remove(type.id().toString());
        else {
            var encoded = type.persistentCodec().encodeStart(NbtOps.INSTANCE, value).result()
                    .orElseThrow(() -> new IllegalArgumentException("Could not encode entity component " + type.id()));
            components.put(type.id().toString(), encoded);
        }
        if (components.isEmpty()) data.remove(ROOT_TAG);
        else data.put(ROOT_TAG, components);
    }
}
