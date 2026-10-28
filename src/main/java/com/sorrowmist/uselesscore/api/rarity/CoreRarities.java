package com.sorrowmist.uselesscore.api.rarity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;

import java.util.function.UnaryOperator;

/** Forge 1.20.1 custom rarity helpers shared by Useless mods. */
public final class CoreRarities {
    public static final Rarity MYTHIC = create("mythic", style -> style.withColor(ChatFormatting.GOLD));
    public static final Rarity LEGENDARY = create("legendary", style -> style.withColor(ChatFormatting.RED));

    private CoreRarities() {}

    public static Rarity create(String name, UnaryOperator<Style> styleModifier) {
        return Rarity.create(name, styleModifier);
    }
}
