package com.sorrowmist.uselesscore.api.capability;

import java.util.function.BiFunction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;

/** Registers providers exposed through the block entity's Forge getCapability override. */
public final class CoreCapabilityRegistration {
    public <B extends BlockEntity, T> void registerBlockEntity(CoreBlockCapability<T, Direction> capability,
            BlockEntityType<B> type, BiFunction<? super B, Direction, ? extends T> provider) {
        registerBlockEntity(capability.forgeCapability(), type, provider);
    }
    public <B extends BlockEntity, T> void registerBlockEntity(Capability<T> capability,
            BlockEntityType<B> type, BiFunction<? super B, Direction, ? extends T> provider) {
        CoreCapabilities.registerBlockProvider(type, capability, provider);
    }
}
