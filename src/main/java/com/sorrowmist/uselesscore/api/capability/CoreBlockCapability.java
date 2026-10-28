package com.sorrowmist.uselesscore.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Objects;
import java.util.function.Function;

/** Sided block-capability descriptor backed by Forge's BlockEntity capability system. */
public final class CoreBlockCapability<T, S> {
    private final ResourceLocation id;
    private final Capability<T> capability;
    private final Function<S, net.minecraft.core.Direction> directionMapper;

    private CoreBlockCapability(ResourceLocation id, Capability<T> capability,
                                Function<S, net.minecraft.core.Direction> directionMapper) {
        this.id = Objects.requireNonNull(id, "id");
        this.capability = Objects.requireNonNull(capability, "capability");
        this.directionMapper = Objects.requireNonNull(directionMapper, "directionMapper");
    }

    public static <T> CoreBlockCapability<T, net.minecraft.core.Direction> createSided(
            ResourceLocation id, Capability<T> capability) {
        return new CoreBlockCapability<>(id, capability, direction -> direction);
    }

    public ResourceLocation id() {
        return id;
    }

    public Capability<T> forgeCapability() {
        return capability;
    }

    public T get(Level level, BlockPos pos, S side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return null;
        return blockEntity.getCapability(capability, directionMapper.apply(side)).resolve().orElse(null);
    }
}
