package com.sorrowmist.uselesscore.api.network;

import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkHooks;

public final class CoreMenus {
    private CoreMenus() {}
    public static void open(Player player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
        if (player instanceof ServerPlayer serverPlayer) NetworkHooks.openScreen(serverPlayer, provider, extraData);
    }
}
