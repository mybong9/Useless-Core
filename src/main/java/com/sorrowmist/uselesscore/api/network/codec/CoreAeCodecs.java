package com.sorrowmist.uselesscore.api.network.codec;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.crafting.PatternDetailsHelper;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** AE2 1.20.1 key and stack codecs, which that API does not expose as constants. */
public final class CoreAeCodecs {
    public static final Codec<AEKey> KEY = CompoundTag.CODEC.xmap(AEKey::fromTagGeneric, AEKey::toTagGeneric);
    public static final CoreStreamCodec<FriendlyByteBuf, AEKey> KEY_STREAM = CoreStreamCodec.of(
            (buffer, key) -> AEKey.writeKey(buffer, key), AEKey::readKey);
    public static final Codec<AEItemKey> ITEM_KEY = CompoundTag.CODEC.xmap(AEItemKey::fromTag, AEItemKey::toTag);
    public static final Codec<GenericStack> GENERIC_STACK = CompoundTag.CODEC.xmap(
            GenericStack::readTag, GenericStack::writeTag);
    public static final CoreStreamCodec<FriendlyByteBuf, AEItemKey> ITEM_KEY_STREAM = CoreStreamCodec.of(
            (buffer, key) -> key.writeToPacket(buffer), AEItemKey::fromPacket);
    public static final CoreStreamCodec<FriendlyByteBuf, GenericStack> GENERIC_STACK_STREAM = CoreStreamCodec.of(
            (buffer, stack) -> GenericStack.writeBuffer(stack, buffer), GenericStack::readBuffer);

    /** Tests and reads the NBT-backed processing-pattern payload used by AE2 1.20.1. */
    public static boolean hasProcessingPatternPayload(ItemStack stack) {
        CompoundTag tag = stack == null ? null : stack.getTag();
        return tag != null && !tag.contains("recipe", Tag.TAG_STRING)
                && tag.contains("in", Tag.TAG_LIST) && tag.contains("out", Tag.TAG_LIST);
    }

    public static boolean hasProcessingPatternPayload(AEItemKey definition) {
        return definition != null && hasProcessingPatternPayload(definition.toStack(1));
    }

    /** Reads a processing pattern's concrete inputs from the AE2 1.20.1 NBT payload. */
    public static List<GenericStack> processingInputs(AEItemKey definition) {
        return processingStacks(definition, "in");
    }

    /** Reads a processing pattern's concrete outputs from the AE2 1.20.1 NBT payload. */
    public static List<GenericStack> processingOutputs(AEItemKey definition) {
        return processingStacks(definition, "out");
    }

    private static List<GenericStack> processingStacks(AEItemKey definition, String key) {
        if (!hasProcessingPatternPayload(definition)) return List.of();
        CompoundTag tag = definition.getTag();
        ListTag encoded = tag.getList(key, Tag.TAG_COMPOUND);
        List<GenericStack> stacks = new ArrayList<>(encoded.size());
        for (int i = 0; i < encoded.size(); i++) {
            CompoundTag entry = encoded.getCompound(i);
            if (entry.isEmpty()) continue;
            try {
                GenericStack decoded = GenericStack.readTag(entry);
                if (decoded != null && decoded.what() != null && decoded.amount() > 0) {
                    stacks.add(decoded);
                }
            } catch (RuntimeException ignored) {
                // Keep the valid slots of a partially corrupted pattern searchable.
            }
        }
        return List.copyOf(stacks);
    }

    /** Replaces only processing-pattern outputs, retaining the pattern's other custom NBT. */
    public static boolean setProcessingPatternOutputs(ItemStack target, List<GenericStack> outputs) {
        if (!hasProcessingPatternPayload(target) || outputs == null || outputs.isEmpty()) return false;
        ListTag encoded = new ListTag();
        boolean hasOutput = false;
        for (GenericStack output : outputs) {
            encoded.add(GenericStack.writeTag(output));
            hasOutput |= output != null && output.what() != null && output.amount() > 0;
        }
        if (!hasOutput) return false;
        target.getOrCreateTag().put("out", encoded);
        return true;
    }

    /** Reads the encoded inputs and outputs for each AE2 pattern kind supported by 1.20.1. */
    public static Optional<PatternData> patternData(AEItemKey definition) {
        if (definition == null || definition.getTag() == null) return Optional.empty();
        CompoundTag tag = definition.getTag();
        if (hasProcessingPatternPayload(definition)) {
            return Optional.of(new PatternData(Optional.empty(), processingInputs(definition),
                    processingOutputs(definition)));
        }

        Optional<CraftingPatternData> crafting = craftingPatternData(definition);
        if (crafting.isPresent()) {
            CraftingPatternData data = crafting.get();
            List<GenericStack> inputs = data.inputs().stream()
                    .map(GenericStack::fromItemStack).filter(java.util.Objects::nonNull).toList();
            GenericStack output = GenericStack.fromItemStack(data.output());
            return Optional.of(new PatternData(Optional.of(data.recipeId()), inputs,
                    output == null ? List.of() : List.of(output)));
        }

        ResourceLocation recipeId = tag.contains("recipe", Tag.TAG_STRING)
                ? ResourceLocation.tryParse(tag.getString("recipe")) : null;
        if (tag.contains("in", Tag.TAG_COMPOUND) && tag.contains("out", Tag.TAG_COMPOUND)) {
            AEItemKey input = AEItemKey.fromTag(tag.getCompound("in"));
            AEItemKey output = AEItemKey.fromTag(tag.getCompound("out"));
            return Optional.of(new PatternData(Optional.ofNullable(recipeId),
                    input == null ? List.of() : List.of(new GenericStack(input, 1)),
                    output == null ? List.of() : List.of(new GenericStack(output, 1))));
        }

        if (tag.contains("template", Tag.TAG_COMPOUND) && tag.contains("base", Tag.TAG_COMPOUND)
                && tag.contains("addition", Tag.TAG_COMPOUND) && tag.contains("out", Tag.TAG_COMPOUND)) {
            List<GenericStack> inputs = new ArrayList<>(3);
            addItemKey(inputs, tag.getCompound("template"));
            addItemKey(inputs, tag.getCompound("base"));
            addItemKey(inputs, tag.getCompound("addition"));
            AEItemKey output = AEItemKey.fromTag(tag.getCompound("out"));
            return Optional.of(new PatternData(Optional.ofNullable(recipeId), inputs,
                    output == null ? List.of() : List.of(new GenericStack(output, 1))));
        }
        return Optional.empty();
    }

    private static void addItemKey(List<GenericStack> stacks, CompoundTag tag) {
        AEItemKey key = AEItemKey.fromTag(tag);
        if (key != null) stacks.add(new GenericStack(key, 1));
    }

    public record PatternData(Optional<ResourceLocation> recipeId, List<GenericStack> inputs,
                              List<GenericStack> outputs) {
        public PatternData {
            recipeId = recipeId == null ? Optional.empty() : recipeId;
            inputs = List.copyOf(inputs);
            outputs = List.copyOf(outputs);
        }
    }

    /** Writes an AE2 processing pattern using the target-version NBT implementation. */
    public static ItemStack encodeProcessingPattern(List<GenericStack> inputs, List<GenericStack> outputs) {
        return PatternDetailsHelper.encodeProcessingPattern(
                inputs.toArray(GenericStack[]::new), outputs.toArray(GenericStack[]::new));
    }

    /** Copies the raw AE2 processing payload into another item stack without losing the NBT data. */
    public static boolean copyProcessingPatternPayload(ItemStack source, ItemStack target) {
        if (!hasProcessingPatternPayload(source) || target == null || target.isEmpty()) return false;
        target.setTag(source.getTag().copy());
        return true;
    }

    /** Sets the legacy AE2 quantum-entangled-singularity frequency tag used by 1.20.1. */
    public static void setEntangledSingularityFrequency(ItemStack stack, long frequency) {
        if (stack == null || stack.isEmpty()) return;
        stack.getOrCreateTag().putLong("freq", frequency);
    }

    /** Extracts the stable recipe identity and display grid from an AE2 1.20.1 crafting pattern. */
    public static Optional<CraftingPatternData> craftingPatternData(ItemStack stack) {
        return craftingPatternData(stack == null ? null : AEItemKey.of(stack));
    }

    public static Optional<CraftingPatternData> craftingPatternData(AEItemKey definition) {
        if (definition == null || definition.getTag() == null) return Optional.empty();
        CompoundTag tag = definition.getTag();
        if (!tag.contains("recipe", Tag.TAG_STRING) || !tag.contains("in", Tag.TAG_LIST)
                || !tag.contains("out", Tag.TAG_COMPOUND)) return Optional.empty();
        ResourceLocation recipeId = ResourceLocation.tryParse(tag.getString("recipe"));
        if (recipeId == null) return Optional.empty();

        ListTag inputTag = tag.getList("in", Tag.TAG_COMPOUND);
        List<ItemStack> inputs = new ArrayList<>(inputTag.size());
        for (int i = 0; i < inputTag.size(); i++) {
            inputs.add(ItemStack.of(inputTag.getCompound(i)));
        }
        ItemStack output = ItemStack.of(tag.getCompound("out"));
        if (output.isEmpty()) return Optional.empty();
        return Optional.of(new CraftingPatternData(recipeId, inputs, output,
                tag.getBoolean("substitute"), tag.getBoolean("substituteFluids")));
    }

    public record CraftingPatternData(ResourceLocation recipeId, List<ItemStack> inputs, ItemStack output,
                                     boolean canSubstitute, boolean canSubstituteFluids) {
        public CraftingPatternData {
            inputs = inputs.stream().map(ItemStack::copy).toList();
            output = output.copy();
        }
    }

    private CoreAeCodecs() {
    }
}
