package com.sorrowmist.uselesscore.api.component;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import appeng.api.stacks.AEItemKey;

/** NBT-backed item component accessors for Forge/Minecraft 1.20.1. */
public final class CoreItemComponents {
    private static final String ROOT_TAG = "UselessCoreComponents";

    private CoreItemComponents() {
    }

    public static <T> T get(ItemStack stack, CoreDataComponentHolder<T> holder) {
        return get(stack, holder.get());
    }

    public static <T> T get(AEItemKey key, CoreDataComponentType<T> type) {
        return key == null ? null : get(key.toStack(), type);
    }

    public static <T> T get(AEItemKey key, CoreDataComponentHolder<T> holder) {
        return key == null ? null : get(key.toStack(), holder.get());
    }

    public static <T> T get(ItemStack stack, CoreDataComponentType<T> type) {
        CompoundTag components = stack.getTagElement(ROOT_TAG);
        if (components == null || !components.contains(type.id().toString())) return null;
        return decode(type.persistentCodec(), components.get(type.id().toString()));
    }

    public static <T> T getOrDefault(ItemStack stack, CoreDataComponentHolder<T> holder, T fallback) {
        return getOrDefault(stack, holder.get(), fallback);
    }

    public static <T> T getOrDefault(ItemStack stack, CoreDataComponentType<T> type, T fallback) {
        T value = get(stack, type);
        return value == null ? fallback : value;
    }

    public static <T> void set(ItemStack stack, CoreDataComponentHolder<T> holder, T value) {
        set(stack, holder.get(), value);
    }

    public static <T> void set(ItemStack stack, CoreDataComponentType<T> type, T value) {
        if (value == null) {
            remove(stack, type);
            return;
        }
        var encoded = type.persistentCodec().encodeStart(NbtOps.INSTANCE, value).result()
                .orElseThrow(() -> new IllegalArgumentException("Could not encode component " + type.id()));
        CompoundTag itemTag = stack.getOrCreateTag();
        CompoundTag components = itemTag.getCompound(ROOT_TAG);
        components.put(type.id().toString(), encoded);
        itemTag.put(ROOT_TAG, components);
    }

    public static void remove(ItemStack stack, CoreDataComponentHolder<?> holder) {
        remove(stack, holder.get());
    }

    public static void remove(ItemStack stack, CoreDataComponentType<?> type) {
        CompoundTag root = stack.getTagElement(ROOT_TAG);
        if (root == null) return;
        root.remove(type.id().toString());
        if (root.isEmpty()) stack.removeTagKey(ROOT_TAG);
    }

    public static boolean has(ItemStack stack, CoreDataComponentHolder<?> holder) {
        return has(stack, holder.get());
    }

    public static boolean has(ItemStack stack, CoreDataComponentType<?> type) {
        CompoundTag components = stack.getTagElement(ROOT_TAG);
        return components != null && components.contains(type.id().toString());
    }

    private static <T> T decode(Codec<T> codec, net.minecraft.nbt.Tag value) {
        return codec.parse(NbtOps.INSTANCE, value).result().orElse(null);
    }
}
