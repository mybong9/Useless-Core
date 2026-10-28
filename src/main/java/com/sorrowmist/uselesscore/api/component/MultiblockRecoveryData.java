package com.sorrowmist.uselesscore.api.component;

import appeng.api.stacks.GenericStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import com.sorrowmist.uselesscore.api.network.codec.CoreByteBufCodecs;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;

import java.util.List;

public record MultiblockRecoveryData(
        int version, long energy, List<GenericStack> contents, long automaticEnergyLimit) {
    private static final int LEGACY_DEFAULT_VERSION = 1;
    public static final int CURRENT_VERSION = 2;
    public static final Codec<MultiblockRecoveryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("version", LEGACY_DEFAULT_VERSION).forGetter(MultiblockRecoveryData::version),
            Codec.LONG.optionalFieldOf("energy", 0L).forGetter(MultiblockRecoveryData::energy),
            com.sorrowmist.uselesscore.api.network.codec.CoreAeCodecs.GENERIC_STACK.listOf()
                    .optionalFieldOf("contents", List.of()).forGetter(MultiblockRecoveryData::contents),
            Codec.LONG.optionalFieldOf("automatic_energy_limit", Long.MAX_VALUE)
                    .forGetter(MultiblockRecoveryData::automaticEnergyLimit)
    ).apply(instance, MultiblockRecoveryData::new));
    public static final CoreStreamCodec<FriendlyByteBuf, MultiblockRecoveryData> STREAM_CODEC =
            CoreStreamCodec.composite(
                    CoreByteBufCodecs.VAR_INT, MultiblockRecoveryData::version,
                    CoreByteBufCodecs.VAR_LONG, MultiblockRecoveryData::energy,
                    com.sorrowmist.uselesscore.api.network.codec.CoreAeCodecs.GENERIC_STACK_STREAM
                            .apply(CoreByteBufCodecs.list()), MultiblockRecoveryData::contents,
                    CoreByteBufCodecs.VAR_LONG, MultiblockRecoveryData::automaticEnergyLimit,
                    MultiblockRecoveryData::new);

    public MultiblockRecoveryData {
        energy = Math.max(0L, energy);
        contents = List.copyOf(contents == null ? List.of() : contents);
        automaticEnergyLimit = Math.max(0L, automaticEnergyLimit);
    }

    public MultiblockRecoveryData(int version, long energy, List<GenericStack> contents) {
        this(version, energy, contents, Long.MAX_VALUE);
    }

    public boolean isEmpty() {
        return energy <= 0L && contents.isEmpty() && automaticEnergyLimit == Long.MAX_VALUE;
    }
}
