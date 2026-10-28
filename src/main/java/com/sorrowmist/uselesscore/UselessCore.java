package com.sorrowmist.uselesscore;

import net.minecraftforge.fml.common.Mod;

@Mod(UselessCore.MOD_ID)
public final class UselessCore {
    public static final String MOD_ID = "useless_core";

    public UselessCore() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addGenericListener(
                net.minecraft.world.level.block.entity.BlockEntity.class,
                com.sorrowmist.uselesscore.api.capability.CoreCapabilities::attach);
        if (net.minecraftforge.fml.ModList.get().isLoaded("ae2")) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(UselessCore::onServerStopped);
        }
    }

    private static void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        com.sorrowmist.uselesscore.api.crafting.CoreCraftingJobs.clearAll();
    }
}
