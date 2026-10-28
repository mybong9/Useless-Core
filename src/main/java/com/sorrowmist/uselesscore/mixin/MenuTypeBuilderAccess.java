package com.sorrowmist.uselesscore.mixin;

import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(value = MenuTypeBuilder.class, remap = false)
public interface MenuTypeBuilderAccess {
    @Accessor("id") void core$setId(ResourceLocation id);
    @Accessor("menuType") void core$setMenuType(MenuType<?> type);
    @Invoker("fromNetwork") AEBaseMenu core$fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer);
    @Invoker("open") boolean core$open(Player player, MenuLocator locator, boolean returnTo);
}
