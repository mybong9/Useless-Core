package com.sorrowmist.uselesscore.api.registry;

import net.minecraft.server.MinecraftServer;

/** Timing data exposed by Minecraft 1.20.1's fixed twenty-tick server loop. */
public final class CoreServerTiming {
    private CoreServerTiming() {}
    public static long[] tickTimesNanos(MinecraftServer server) { return server.tickTimes; }
    public static double targetMillisPerTick(MinecraftServer server) { return 50.0D; }
}
