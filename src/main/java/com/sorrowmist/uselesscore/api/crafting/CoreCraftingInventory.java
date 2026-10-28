package com.sorrowmist.uselesscore.api.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Forge 1.20.1 crafting-grid adapter for APIs that used 1.21's immutable CraftingInput. */
public final class CoreCraftingInventory {
    private static final AbstractContainerMenu DETACHED_MENU = new AbstractContainerMenu(null, 0) {
        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    };

    private CoreCraftingInventory() {
    }

    public static TransientCraftingContainer create(int width, int height, List<ItemStack> stacks) {
        if (width <= 0 || height <= 0 || (long) width * height > 256) {
            throw new IllegalArgumentException("Invalid crafting-grid dimensions: " + width + "x" + height);
        }
        NonNullList<ItemStack> items = NonNullList.withSize(width * height, ItemStack.EMPTY);
        for (int slot = 0; slot < Math.min(items.size(), stacks.size()); slot++) {
            ItemStack stack = stacks.get(slot);
            items.set(slot, stack == null ? ItemStack.EMPTY : stack.copy());
        }
        return new TransientCraftingContainer(DETACHED_MENU, width, height, items);
    }
}
