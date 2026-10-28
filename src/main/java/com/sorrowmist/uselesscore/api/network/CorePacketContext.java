package com.sorrowmist.uselesscore.api.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/** Small client/server-neutral packet context matching the 1.21 payload handler shape. */
public final class CorePacketContext {
    private final NetworkEvent.Context context;

    CorePacketContext(NetworkEvent.Context context) {
        this.context = context;
    }

    public Player player() {
        var sender = context.getSender();
        return sender != null ? sender : Minecraft.getInstance().player;
    }

    public void enqueueWork(Runnable work) {
        context.enqueueWork(work);
    }
}
