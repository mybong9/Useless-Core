package com.sorrowmist.uselesscore.api.network;

import net.minecraft.resources.ResourceLocation;

/** Packet identity contract used in place of the 1.20.2+ custom payload API. */
public interface CoreCustomPacketPayload {
    Type<? extends CoreCustomPacketPayload> type();

    record Type<T extends CoreCustomPacketPayload>(ResourceLocation id) {
    }
}
