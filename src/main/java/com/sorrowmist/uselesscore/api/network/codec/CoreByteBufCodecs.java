package com.sorrowmist.uselesscore.api.network.codec;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;

/** FriendlyByteBuf codecs replacing the 1.21 ByteBufCodecs helpers. */
public final class CoreByteBufCodecs {
    public static final CoreStreamCodec<FriendlyByteBuf, Boolean> BOOL = CoreStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
    public static final CoreStreamCodec<FriendlyByteBuf, Integer> INT = CoreStreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt);
    public static final CoreStreamCodec<FriendlyByteBuf, Integer> VAR_INT = CoreStreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
    public static final CoreStreamCodec<FriendlyByteBuf, Long> VAR_LONG = CoreStreamCodec.of(FriendlyByteBuf::writeVarLong, FriendlyByteBuf::readVarLong);
    public static final CoreStreamCodec<FriendlyByteBuf, Float> FLOAT = CoreStreamCodec.of(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
    public static final CoreStreamCodec<FriendlyByteBuf, String> STRING_UTF8 = CoreStreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
    public static final CoreStreamCodec<FriendlyByteBuf, CompoundTag> COMPOUND_TAG = CoreStreamCodec.of(FriendlyByteBuf::writeNbt, FriendlyByteBuf::readNbt);
    public static final CoreStreamCodec<FriendlyByteBuf, UUID> UUID = CoreStreamCodec.of(FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
    public static final CoreStreamCodec<FriendlyByteBuf, ResourceLocation> RESOURCE_LOCATION = CoreStreamCodec.of(FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation);
    public static final CoreStreamCodec<FriendlyByteBuf, BlockPos> BLOCK_POS = CoreStreamCodec.of(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);
    public static final CoreStreamCodec<FriendlyByteBuf, ItemStack> ITEM_STACK = CoreStreamCodec.of(FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem);
    public static final CoreStreamCodec<FriendlyByteBuf, ItemStack> OPTIONAL_ITEM_STACK = CoreStreamCodec.of(
            (buf, stack) -> { buf.writeBoolean(stack != null && !stack.isEmpty()); if (stack != null && !stack.isEmpty()) buf.writeItem(stack); },
            buf -> buf.readBoolean() ? buf.readItem() : ItemStack.EMPTY);
    public static final CoreStreamCodec<FriendlyByteBuf, FluidStack> FLUID_STACK = CoreStreamCodec.of(
            (buf, stack) -> stack.writeToPacket(buf), buf -> FluidStack.readFromPacket(buf));
    public static final CoreStreamCodec<FriendlyByteBuf, GlobalPos> GLOBAL_POS = CoreStreamCodec.of(
            (buf, value) -> { buf.writeResourceLocation(value.dimension().location()); buf.writeBlockPos(value.pos()); },
            buf -> GlobalPos.of(ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()), buf.readBlockPos()));

    private CoreByteBufCodecs() {
    }

    public static <T> Function<CoreStreamCodec<FriendlyByteBuf, T>, CoreStreamCodec<FriendlyByteBuf, List<T>>> list() {
        return list(1_000_000);
    }

    public static <T> Function<CoreStreamCodec<FriendlyByteBuf, T>, CoreStreamCodec<FriendlyByteBuf, List<T>>> list(int maximum) {
        if (maximum < 0) throw new IllegalArgumentException("Negative collection limit");
        return element -> CoreStreamCodec.of((buf, values) -> {
            if (values.size() > maximum) throw new IllegalArgumentException("Collection exceeds limit: " + maximum);
            buf.writeVarInt(values.size());
            for (T value : values) element.encode(buf, value);
        }, buf -> {
            int size = buf.readVarInt();
            if (size < 0 || size > maximum) throw new IllegalArgumentException("Invalid collection size: " + size);
            List<T> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) values.add(element.decode(buf));
            return List.copyOf(values);
        });
    }

    public static CoreStreamCodec<FriendlyByteBuf, List<UUID>> uuidList() {
        return CoreStreamCodec.of((buf, values) -> {
            buf.writeVarInt(values.size());
            for (UUID value : values) buf.writeUUID(value);
        }, buf -> {
            int size = buf.readVarInt();
            if (size < 0 || size > 4096) throw new IllegalArgumentException("Invalid UUID count: " + size);
            List<UUID> values = new ArrayList<>(size);
            for (int i = 0; i < size; i++) values.add(buf.readUUID());
            return List.copyOf(values);
        });
    }

    public static <T, C extends Collection<T>> Function<CoreStreamCodec<FriendlyByteBuf, T>, CoreStreamCodec<FriendlyByteBuf, C>> collection(IntFunction<C> factory) {
        return element -> CoreStreamCodec.of((buf, values) -> {
            buf.writeVarInt(values.size());
            for (T value : values) element.encode(buf, value);
        }, buf -> {
            int size = buf.readVarInt();
            if (size < 0 || size > 1_000_000) throw new IllegalArgumentException("Invalid collection size: " + size);
            C values = factory.apply(size);
            for (int i = 0; i < size; i++) values.add(element.decode(buf));
            return values;
        });
    }

    public static <T, C extends Collection<T>> CoreStreamCodec<FriendlyByteBuf, C> collection(
            IntFunction<C> factory, CoreStreamCodec<FriendlyByteBuf, T> element) {
        return collection(factory).apply(element);
    }

    public static <T> CoreStreamCodec<FriendlyByteBuf, T> fromCodec(Codec<T> codec) {
        return CoreStreamCodec.of((buf, value) -> {
            var encoded = codec.encodeStart(NbtOps.INSTANCE, value).result()
                    .orElseThrow(() -> new IllegalArgumentException("Codec encode failed"));
            buf.writeNbt(encoded instanceof CompoundTag tag ? tag : wrap(encoded));
        }, buf -> {
            CompoundTag encoded = buf.readNbt();
            if (encoded == null) throw new IllegalArgumentException("Missing codec payload");
            var input = encoded.contains("value") ? encoded.get("value") : encoded;
            return codec.parse(NbtOps.INSTANCE, input).result()
                    .orElseThrow(() -> new IllegalArgumentException("Codec decode failed"));
        });
    }

    public static <T> CoreStreamCodec<FriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
        return fromCodec(codec);
    }

    private static CompoundTag wrap(net.minecraft.nbt.Tag tag) {
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("value", tag);
        return wrapper;
    }
}
