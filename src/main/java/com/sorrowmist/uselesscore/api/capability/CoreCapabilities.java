package com.sorrowmist.uselesscore.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/** Common capability lookups with a Forge 1.20.1 implementation. */
public final class CoreCapabilities {
    public static final class ItemHandler {
        public static final Capability<IItemHandler> BLOCK = ForgeCapabilities.ITEM_HANDLER;
        public static final Capability<IItemHandler> ITEM = ForgeCapabilities.ITEM_HANDLER;
        private ItemHandler() {}
    }

    public static final class FluidHandler {
        public static final Capability<IFluidHandler> BLOCK = ForgeCapabilities.FLUID_HANDLER;
        public static final Capability<IFluidHandlerItem> ITEM = ForgeCapabilities.FLUID_HANDLER_ITEM;
        private FluidHandler() {}
    }

    public static final class EnergyStorage {
        public static final Capability<IEnergyStorage> BLOCK = ForgeCapabilities.ENERGY;
        private EnergyStorage() {}
    }

    public static final class KeyStorage {
        public static final Capability<appeng.api.storage.MEStorage> BLOCK =
                net.minecraftforge.common.capabilities.CapabilityManager.get(new net.minecraftforge.common.capabilities.CapabilityToken<>() {});
        private KeyStorage() {}
    }
    public static final class LongEnergy {
        public static final Capability<com.sorrowmist.uselesscore.api.logistics.LongEnergyHandler> BLOCK =
                net.minecraftforge.common.capabilities.CapabilityManager.get(new net.minecraftforge.common.capabilities.CapabilityToken<>() {});
        private LongEnergy() {}
    }
    /** Reuse capabilities already registered by AE2, Forge auto-registration or another addon. */
    public static synchronized <T> void registerIfAbsent(
            net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent event, Capability<T> capability, Class<T> type) {
        if (!capability.isRegistered()) event.register(type);
    }
    public static void registerNetworkCapabilities(net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent event) {
        registerIfAbsent(event,KeyStorage.BLOCK,appeng.api.storage.MEStorage.class);
        registerIfAbsent(event,LongEnergy.BLOCK,com.sorrowmist.uselesscore.api.logistics.LongEnergyHandler.class);
    }

    private CoreCapabilities() {}

    public static void attach(net.minecraftforge.event.AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity entity = event.getObject();
        boolean registered = TYPE_PROVIDERS.containsKey(entity.getType());
        for (Class<?> type = entity.getClass(); !registered && type != null; type = type.getSuperclass()) {
            registered = BLOCK_PROVIDERS.containsKey(type);
        }
        if (!registered) return;
        event.addCapability(new net.minecraft.resources.ResourceLocation("useless_core", "block_providers"),
                new net.minecraftforge.common.capabilities.ICapabilityProvider() {
                    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
                        return getRegisteredBlockProvider(entity, capability, side);
                    }
                });
        event.addListener(() -> invalidateBlockProviders(entity));
    }

    private static final Map<Class<? extends BlockEntity>, Map<Capability<?>, BiFunction<BlockEntity, Direction, ?>>> BLOCK_PROVIDERS =
            new ConcurrentHashMap<>();
    private static final Map<net.minecraft.world.level.block.entity.BlockEntityType<?>,
            Map<Capability<?>, BiFunction<BlockEntity, Direction, ?>>> TYPE_PROVIDERS = new ConcurrentHashMap<>();
    private static final Map<BlockEntity, Map<Capability<?>, Map<Direction, LazyOptional<?>>>> BLOCK_PROVIDER_CACHE =
            new WeakHashMap<>();

    /**
     * Registers a block-entity capability provider in Forge's 1.20.1 model.
     * Forge providers are exposed from {@link BlockEntity#getCapability}, not from
     * {@code RegisterCapabilitiesEvent.registerBlockEntity} (which is a NeoForge API).
     */
    public static <B extends BlockEntity, T> void registerBlockProvider(
            Class<B> blockEntityType, Capability<T> capability,
            BiFunction<? super B, Direction, ? extends T> provider) {
        BLOCK_PROVIDERS.computeIfAbsent(blockEntityType, ignored -> new ConcurrentHashMap<>())
                .put(capability, (blockEntity, side) -> provider.apply(blockEntityType.cast(blockEntity), side));
    }

    public static <B extends BlockEntity, T> void registerBlockProvider(
            net.minecraft.world.level.block.entity.BlockEntityType<B> type, Capability<T> capability,
            BiFunction<? super B, Direction, ? extends T> provider) {
        TYPE_PROVIDERS.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>()).put(capability,
                (entity, side) -> {
                    @SuppressWarnings("unchecked") B typed = (B) entity;
                    return provider.apply(typed, side);
                });
    }

    /** Returns the Core-registered provider value for an entity, if one is registered. */
    public static <T> LazyOptional<T> getRegisteredBlockProvider(
            BlockEntity blockEntity, Capability<T> capability, Direction side) {
        synchronized (BLOCK_PROVIDER_CACHE) {
            var entityCache = BLOCK_PROVIDER_CACHE.get(blockEntity);
            if (entityCache != null) {
                var sideCache = entityCache.get(capability);
                if (sideCache != null) {
                    @SuppressWarnings("unchecked") LazyOptional<T> cached =
                            (LazyOptional<T>) sideCache.get(side);
                    if (cached != null) return cached;
                }
            }
        }
        var typed = TYPE_PROVIDERS.get(blockEntity.getType());
        if (typed != null && typed.containsKey(capability)) {
            return cachedProvider(blockEntity, capability, side, typed.get(capability));
        }
        for (Class<?> type = blockEntity.getClass(); type != null && BlockEntity.class.isAssignableFrom(type);
             type = type.getSuperclass()) {
            Map<Capability<?>, BiFunction<BlockEntity, Direction, ?>> providers = BLOCK_PROVIDERS.get(type);
            if (providers == null) continue;
            BiFunction<BlockEntity, Direction, ?> provider = providers.get(capability);
            if (provider == null) continue;
            return cachedProvider(blockEntity, capability, side, provider);
        }
        return LazyOptional.empty();
    }

    private static <T> LazyOptional<T> cachedProvider(BlockEntity blockEntity, Capability<T> capability,
            Direction side, BiFunction<BlockEntity, Direction, ?> provider) {
            synchronized (BLOCK_PROVIDER_CACHE) {
                Map<Capability<?>, Map<Direction, LazyOptional<?>>> entityCache =
                        BLOCK_PROVIDER_CACHE.computeIfAbsent(blockEntity, ignored -> new HashMap<>());
                Map<Direction, LazyOptional<?>> sideCache =
                        entityCache.computeIfAbsent(capability, ignored -> new HashMap<>());
                if (sideCache.containsKey(side)) {
                    @SuppressWarnings("unchecked") LazyOptional<T> cached =
                            (LazyOptional<T>) sideCache.get(side);
                    return cached;
                }
                @SuppressWarnings("unchecked") T value = (T) provider.apply(blockEntity, side);
                LazyOptional<T> optional = value == null ? LazyOptional.empty() : LazyOptional.of(() -> value);
                sideCache.put(side, optional);
                return optional;
            }
    }

    /** Invalidates all cached Core providers when the block entity unloads or is removed. */
    public static void invalidateBlockProviders(BlockEntity blockEntity) {
        Map<Capability<?>, Map<Direction, LazyOptional<?>>> entityCache;
        synchronized (BLOCK_PROVIDER_CACHE) {
            entityCache = BLOCK_PROVIDER_CACHE.remove(blockEntity);
        }
        if (entityCache == null) return;
        entityCache.values().forEach(bySide -> bySide.values().forEach(LazyOptional::invalidate));
    }

    public static <T> T get(Level level, Capability<T> capability, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return null;
        LazyOptional<T> optional = blockEntity.getCapability(capability, side);
        return optional.resolve().orElse(null);
    }

    public static <T> T get(Level level, Capability<T> capability, BlockPos pos,
                            Object ignoredState, BlockEntity ignoredBlockEntity, Direction side) {
        return get(level, capability, pos, side);
    }

    public static <T> T get(BlockEntity blockEntity, Capability<T> capability, Direction side) {
        if (blockEntity == null) return null;
        return blockEntity.getCapability(capability, side).resolve().orElse(null);
    }

    public static <T, S> T get(Level level, CoreBlockCapability<T, S> capability,
                               BlockPos pos, S side) {
        return capability.get(level, pos, side);
    }
}
