package com.sorrowmist.uselesscore.api.registry;

import net.minecraft.world.item.Item;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Shared typed registration factories for Forge 1.20.1. */
public final class CoreRegistries {
    private CoreRegistries() {}

    public static <T> net.minecraft.resources.RegistryOps<T> serializationContext(
            HolderLookup.Provider registries, com.mojang.serialization.DynamicOps<T> ops) {
        return net.minecraft.resources.RegistryOps.create(ops, registries);
    }

    public static DeferredRegister<Block> blocks(String namespace) {
        return DeferredRegister.create(ForgeRegistries.BLOCKS, namespace);
    }

    public static DeferredRegister<Item> items(String namespace) {
        return DeferredRegister.create(ForgeRegistries.ITEMS, namespace);
    }

    /** Supplies registry context to shared codecs, including during early block-entity loading. */
    public static HolderLookup.Provider lookup(BlockEntity blockEntity) {
        return blockEntity.getLevel() == null ? RegistryAccess.EMPTY : blockEntity.getLevel().registryAccess();
    }
}
