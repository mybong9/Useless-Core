package com.sorrowmist.uselesscore.api.component;

import com.mojang.serialization.Codec;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraft.resources.ResourceLocation;

/** A small, registry-free component descriptor for values stored in 1.20.1 item NBT. */
public final class CoreDataComponentType<T> {
    private final ResourceLocation id;
    private final Codec<T> persistentCodec;
    private final CoreStreamCodec<?, T> networkCodec;

    private CoreDataComponentType(ResourceLocation id, Codec<T> persistentCodec,
                                  CoreStreamCodec<?, T> networkCodec) {
        this.id = id;
        this.persistentCodec = persistentCodec;
        this.networkCodec = networkCodec;
    }

    public ResourceLocation id() {
        return id;
    }

    public Codec<T> persistentCodec() {
        return persistentCodec;
    }

    public CoreStreamCodec<?, T> networkCodec() {
        return networkCodec;
    }

    public static <T> Builder<T> builder(ResourceLocation id) {
        return new Builder<>(id);
    }

    public static final class Builder<T> {
        private final ResourceLocation id;
        private Codec<T> persistentCodec;
        private CoreStreamCodec<?, T> networkCodec;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder<T> persistent(Codec<T> codec) {
            this.persistentCodec = codec;
            return this;
        }

        public Builder<T> networkSynchronized(CoreStreamCodec<?, T> codec) {
            this.networkCodec = codec;
            return this;
        }

        public CoreDataComponentType<T> build() {
            if (persistentCodec == null) {
                throw new IllegalStateException("Persistent codec is required for " + id);
            }
            return new CoreDataComponentType<>(id, persistentCodec, networkCodec);
        }
    }
}
