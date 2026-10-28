package com.sorrowmist.uselesscore.api.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sorrowmist.uselesscore.api.component.CoreDataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import com.sorrowmist.uselesscore.api.network.codec.CoreByteBufCodecs;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;

import java.util.function.UnaryOperator;

/**
 * 熔炉数据组件 - 用于存储高级合金炉的完整状态到物品中
 * 包含阶级、物品栏、流体、能量等所有数据
 */
public record FurnaceDataComponent(int tier, CompoundTag data) {
    
    // Codec 用于持久化存储
    public static final Codec<FurnaceDataComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("tier").forGetter(FurnaceDataComponent::tier),
            CompoundTag.CODEC.fieldOf("data").forGetter(FurnaceDataComponent::data)
        ).apply(instance, FurnaceDataComponent::new)
    );
    
    // CoreStreamCodec 用于网络同步
    public static final CoreStreamCodec<FriendlyByteBuf, FurnaceDataComponent> STREAM_CODEC = 
        CoreStreamCodec.composite(
                CoreByteBufCodecs.INT,
                FurnaceDataComponent::tier,
                CoreByteBufCodecs.COMPOUND_TAG,
                FurnaceDataComponent::data,
                FurnaceDataComponent::new
        );

    public static UnaryOperator<CoreDataComponentType.Builder<FurnaceDataComponent>> builder() {
        return builder -> builder
            .persistent(CODEC)
            .networkSynchronized(STREAM_CODEC);
    }

    public static FurnaceDataComponent empty() {
        return new FurnaceDataComponent(0, new CompoundTag());
    }
}
