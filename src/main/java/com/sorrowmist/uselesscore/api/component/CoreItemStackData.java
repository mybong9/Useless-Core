package com.sorrowmist.uselesscore.api.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;
import java.util.function.Consumer;

/**
 * Forge/Minecraft 1.20.1 item-data accessors for common 1.21 item-component values.
 * Values are stored in the legacy stack tag using vanilla 1.20 conventions.
 */
public final class CoreItemStackData {
    private static final String CUSTOM_DATA_TAG = "UselessCoreCustomData";
    private static final String ENTITY_TAG = "EntityTag";
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String BLOCK_STATE_TAG = "BlockStateTag";
    private static final String CUSTOM_MODEL_DATA = "CustomModelData";
    private static final String UNBREAKABLE = "Unbreakable";
    private static final String MAX_DAMAGE = "UselessCoreMaxDamage";
    private static final String RARITY = "UselessCoreRarity";

    private CoreItemStackData() {}

    public static CompoundTag dataTag(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
    }

    public static CompoundTag dataTag(net.minecraftforge.fluids.FluidStack stack) {
        return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
    }

    public static void copyData(ItemStack source, ItemStack target) {
        target.setTag(source.hasTag() ? source.getTag().copy() : null);
    }

    public static void copyData(net.minecraftforge.fluids.FluidStack source,
            net.minecraftforge.fluids.FluidStack target) {
        target.setTag(source.hasTag() ? source.getTag().copy() : null);
    }

    public static ItemStack parseOptional(net.minecraft.core.HolderLookup.Provider registries, CompoundTag tag) {
        return ItemStack.of(tag);
    }

    public static java.util.Optional<ItemStack> parse(net.minecraft.core.HolderLookup.Provider registries, CompoundTag tag) {
        ItemStack stack = parseOptional(registries, tag);
        return stack.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(stack);
    }

    /** Namespaced storage keeps user custom data separate from the 1.20 EntityTag and BlockEntityTag fields. */
    public static CompoundTag getCustomData(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(CUSTOM_DATA_TAG);
        return tag == null ? new CompoundTag() : tag.copy();
    }

    public static CompoundTag getOrCreateCustomData(ItemStack stack) {
        return stack.getOrCreateTagElement(CUSTOM_DATA_TAG);
    }

    public static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) removeTag(stack, CUSTOM_DATA_TAG);
        else stack.getOrCreateTag().put(CUSTOM_DATA_TAG, tag.copy());
    }

    /** Applies an in-place edit to the stack's legacy custom-data compound. */
    public static void updateCustomData(ItemStack stack, Consumer<CompoundTag> update) {
        CompoundTag tag = getOrCreateCustomData(stack);
        update.accept(tag);
        if (tag.isEmpty()) removeTag(stack, CUSTOM_DATA_TAG);
    }

    public static CompoundTag getEntityData(ItemStack stack) {
        return getNested(stack, ENTITY_TAG);
    }

    public static void setEntityData(ItemStack stack, CompoundTag tag) {
        setNested(stack, ENTITY_TAG, tag);
    }

    public static CompoundTag getBlockEntityData(ItemStack stack) {
        return getNested(stack, BLOCK_ENTITY_TAG);
    }

    public static void setBlockEntityData(ItemStack stack, CompoundTag tag) {
        setNested(stack, BLOCK_ENTITY_TAG, tag);
    }

    public static CompoundTag getBlockStateData(ItemStack stack) {
        return getNested(stack, BLOCK_STATE_TAG);
    }

    public static void setBlockStateData(ItemStack stack, CompoundTag tag) {
        setNested(stack, BLOCK_STATE_TAG, tag);
    }

    /** Applies vanilla BlockStateTag values, ignoring unknown properties and invalid values. */
    public static BlockState applyBlockStateData(ItemStack stack, BlockState state) {
        CompoundTag properties = getBlockStateData(stack);
        for (String name : properties.getAllKeys()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(name);
            if (property != null) state = applyProperty(state, property, properties.getString(name));
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState applyProperty(
            BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
    }

    public static Component getCustomName(ItemStack stack) {
        return stack.hasCustomHoverName() ? stack.getHoverName() : null;
    }

    public static void setCustomName(ItemStack stack, Component name) {
        if (name == null) stack.resetHoverName();
        else stack.setHoverName(name);
    }

    public static int getCustomModelData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(CUSTOM_MODEL_DATA) ? tag.getInt(CUSTOM_MODEL_DATA) : 0;
    }

    public static void setCustomModelData(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(CUSTOM_MODEL_DATA, value);
    }

    public static boolean isUnbreakable(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(UNBREAKABLE);
    }

    public static void setUnbreakable(ItemStack stack, boolean value) {
        if (value) stack.getOrCreateTag().putBoolean(UNBREAKABLE, true);
        else removeTag(stack, UNBREAKABLE);
    }

    public static int getStoredMaxDamage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(MAX_DAMAGE) ? tag.getInt(MAX_DAMAGE) : stack.getMaxDamage();
    }

    public static void setStoredMaxDamage(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(MAX_DAMAGE, Math.max(0, value));
    }

    /** Returns the visible item rarity, or the vanilla rarity if no override was stored. */
    public static Rarity getRarity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(RARITY)) return stack.getRarity();
        try {
            return Rarity.valueOf(tag.getString(RARITY).toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return stack.getRarity();
        }
    }

    public static void setRarity(ItemStack stack, Rarity rarity) {
        if (rarity == null) removeTag(stack, RARITY);
        else stack.getOrCreateTag().putString(RARITY, rarity.name().toLowerCase(java.util.Locale.ROOT));
    }

    public static boolean hasContainerItems(ItemStack stack) {
        CompoundTag blockEntity = getBlockEntityData(stack);
        return !blockEntity.isEmpty() && blockEntity.contains("Items", net.minecraft.nbt.Tag.TAG_LIST);
    }

    public static boolean hasBundleItems(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("Items", net.minecraft.nbt.Tag.TAG_LIST)
                && stack.getItem() instanceof net.minecraft.world.item.BundleItem;
    }

    public static void copyPresentationData(ItemStack source, ItemStack target) {
        if (source.hasCustomHoverName()) target.setHoverName(source.getHoverName().copy());
        List<Component> lore = getLore(source);
        if (!lore.isEmpty()) setLore(target, lore);
    }

    /** Merges the legacy tag values that represent an item's 1.21 component map. */
    public static void copyAllData(ItemStack source, ItemStack target) {
        CompoundTag sourceTag = source.getTag();
        if (sourceTag != null && !sourceTag.isEmpty()) target.getOrCreateTag().merge(sourceTag.copy());
    }

    public static List<Component> getLore(ItemStack stack) {
        CompoundTag display = stack.getTagElement("display");
        if (display == null || !display.contains("Lore", net.minecraft.nbt.Tag.TAG_LIST)) return List.of();
        ListTag lore = display.getList("Lore", net.minecraft.nbt.Tag.TAG_STRING);
        return lore.stream().<Component>map(tag -> Component.Serializer.fromJson(tag.getAsString()))
                .filter(java.util.Objects::nonNull).toList();
    }

    public static void setLore(ItemStack stack, List<Component> lines) {
        if (lines == null || lines.isEmpty()) {
            CompoundTag display = stack.getTagElement("display");
            if (display != null) {
                display.remove("Lore");
                if (display.isEmpty()) removeTag(stack, "display");
            }
            return;
        }
        ListTag lore = new ListTag();
        for (Component line : lines) lore.add(net.minecraft.nbt.StringTag.valueOf(Component.Serializer.toJson(line)));
        stack.getOrCreateTagElement("display").put("Lore", lore);
    }

    private static CompoundTag getNested(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return new CompoundTag();
        CompoundTag nested = tag.getCompound(key);
        return nested.copy();
    }

    private static void setNested(ItemStack stack, String key, CompoundTag value) {
        if (value == null || value.isEmpty()) removeTag(stack, key);
        else stack.getOrCreateTag().put(key, value.copy());
    }

    private static void removeTag(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(key);
            if (tag.isEmpty()) stack.setTag(null);
        }
    }
}
