package com.sorrowmist.uselesscore.api.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A Forge 1.20.1 fluid ingredient API compatible with the 1.21 recipe surface. */
public abstract class FluidIngredient {
    private record Leaf(String kind, Optional<FluidStack> fluid, Optional<TagKey<Fluid>> tag) {
        private static final Codec<Leaf> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(Leaf::kind),
                CoreFluidStackCodecs.STACK.optionalFieldOf("fluid").forGetter(Leaf::fluid),
                TagKey.codec(Registries.FLUID).optionalFieldOf("tag").forGetter(Leaf::tag)
        ).apply(instance, Leaf::new));
    }

    public static final Codec<FluidIngredient> CODEC = Leaf.CODEC.listOf().xmap(
            FluidIngredient::fromLeaves,
            FluidIngredient::toLeaves);

    public static final CoreStreamCodec<FriendlyByteBuf, FluidIngredient> STREAM_CODEC = CoreStreamCodec.of(
            (buf, ingredient) -> {
                List<Leaf> leaves = ingredient.toLeaves();
                buf.writeVarInt(leaves.size());
                for (Leaf leaf : leaves) {
                    buf.writeUtf(leaf.kind());
                    if (leaf.fluid().isPresent()) leaf.fluid().get().writeToPacket(buf);
                    if (leaf.tag().isPresent()) buf.writeResourceLocation(leaf.tag().get().location());
                }
            },
            buf -> {
                int count = buf.readVarInt();
                if (count < 0 || count > 1024) throw new IllegalArgumentException("Invalid fluid ingredient leaf count: " + count);
                List<Leaf> leaves = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    String kind = buf.readUtf(32);
                    if (kind.equals("single") || kind.equals("components") || kind.equals("partial_components")) {
                        leaves.add(new Leaf(kind, Optional.of(FluidStack.readFromPacket(buf)), Optional.empty()));
                    } else if (kind.equals("tag")) {
                        ResourceLocation id = buf.readResourceLocation();
                        leaves.add(new Leaf(kind, Optional.empty(), Optional.of(TagKey.create(Registries.FLUID, id))));
                    } else {
                        throw new IllegalArgumentException("Unknown fluid ingredient type: " + kind);
                    }
                }
                return fromLeaves(leaves);
            });

    public static FluidIngredient single(FluidStack stack) {
        return new SingleFluidIngredient(stack);
    }

    public static FluidIngredient single(Fluid fluid) {
        return single(new FluidStack(fluid, 1));
    }

    public static FluidIngredient tag(TagKey<Fluid> tag) {
        return new TagFluidIngredient(tag);
    }

    public static FluidIngredient anyOf(List<? extends FluidIngredient> ingredients) {
        if (ingredients.size() == 1) return ingredients.get(0);
        return new CompoundFluidIngredient(ingredients);
    }

    public static FluidIngredient anyOf(FluidIngredient... ingredients) {
        return anyOf(List.of(ingredients));
    }

    public abstract boolean test(FluidStack stack);

    /** Whether matching depends only on fluid identity, allowing identity-based indexes. */
    public boolean isSimple() {
        if (this instanceof SingleFluidIngredient || this instanceof TagFluidIngredient) return true;
        return this instanceof CompoundFluidIngredient && children().stream().allMatch(FluidIngredient::isSimple);
    }

    public abstract List<FluidStack> getStacksList();

    public final FluidStack[] getStacks() {
        return getStacksList().stream().map(FluidStack::copy).toArray(FluidStack[]::new);
    }

    public final boolean isEmpty() {
        return getStacksList().isEmpty();
    }

    public List<FluidIngredient> children() {
        return List.of();
    }

    public Optional<TagKey<Fluid>> tagKey() {
        return Optional.empty();
    }

    private List<Leaf> toLeaves() {
        if (this instanceof CompoundFluidIngredient compound) {
            return compound.children().stream().flatMap(child -> child.toLeaves().stream()).toList();
        }
        if (this instanceof TagFluidIngredient tagged) {
            return List.of(new Leaf("tag", Optional.empty(), Optional.of(tagged.tag())));
        }
        if (this instanceof DataComponentFluidIngredient component) {
            return List.of(new Leaf(component.isStrict() ? "components" : "partial_components",
                    Optional.of(component.stack()), Optional.empty()));
        }
        if (this instanceof SingleFluidIngredient single) {
            return List.of(new Leaf("single", Optional.of(single.stack()), Optional.empty()));
        }
        throw new IllegalStateException("Unsupported fluid ingredient: " + getClass().getName());
    }

    private static FluidIngredient fromLeaves(List<Leaf> leaves) {
        List<FluidIngredient> ingredients = new ArrayList<>(leaves.size());
        for (Leaf leaf : leaves) {
            switch (leaf.kind()) {
                case "single" -> ingredients.add(single(leaf.fluid().orElseThrow()));
                case "components" -> ingredients.add(DataComponentFluidIngredient.of(true, leaf.fluid().orElseThrow()));
                case "partial_components" -> ingredients.add(DataComponentFluidIngredient.of(false, leaf.fluid().orElseThrow()));
                case "tag" -> ingredients.add(tag(leaf.tag().orElseThrow()));
                default -> throw new IllegalArgumentException("Unknown fluid ingredient type: " + leaf.kind());
            }
        }
        return anyOf(ingredients);
    }

    public static List<FluidStack> resolveTag(TagKey<Fluid> tag) {
        return ForgeRegistries.FLUIDS.getValues().stream()
                .filter(fluid -> fluid.builtInRegistryHolder().is(tag))
                .map(fluid -> new FluidStack(fluid, 1))
                .toList();
    }
}
