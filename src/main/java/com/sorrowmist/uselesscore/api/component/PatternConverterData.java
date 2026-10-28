package com.sorrowmist.uselesscore.api.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import com.sorrowmist.uselesscore.api.network.codec.CoreByteBufCodecs;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;

/** Persistent inventory data for the omniversal pattern converter. */
public record PatternConverterData(CompoundTag data) {
    public static final Codec<PatternConverterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.fieldOf("data").forGetter(PatternConverterData::data)
    ).apply(instance, PatternConverterData::new));

    public static final CoreStreamCodec<FriendlyByteBuf, PatternConverterData> STREAM_CODEC =
            CoreStreamCodec.composite(
                    CoreByteBufCodecs.COMPOUND_TAG, PatternConverterData::data,
                    PatternConverterData::new);

    public PatternConverterData {
        data = data == null ? new CompoundTag() : data.copy();
    }

    @Override
    public CompoundTag data() {
        return data.copy();
    }
}
