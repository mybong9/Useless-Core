package com.sorrowmist.uselesscore.api.network;

import com.sorrowmist.uselesscore.api.network.codec.CoreStreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;

/** Forge 1.20.1 SimpleChannel adapter with a 1.21-like payload registration surface. */
public final class CoreNetworkChannel {
    private static final String PROTOCOL = "1";

    private final SimpleChannel channel;
    private int nextMessageId;

    public CoreNetworkChannel(ResourceLocation id) {
        this(id, PROTOCOL);
    }

    public CoreNetworkChannel(ResourceLocation id, String protocol) {
        this.channel = NetworkRegistry.newSimpleChannel(id, () -> protocol,
                protocol::equals, protocol::equals);
    }

    public <T extends CoreCustomPacketPayload> void registerToServer(
            Class<T> type, CoreStreamCodec<FriendlyByteBuf, T> codec,
            BiConsumer<T, CorePacketContext> handler) {
        register(type, codec, handler, NetworkDirection.PLAY_TO_SERVER);
    }

    public <T extends CoreCustomPacketPayload> void registerToClient(
            Class<T> type, CoreStreamCodec<FriendlyByteBuf, T> codec,
            BiConsumer<T, CorePacketContext> handler) {
        register(type, codec, handler, NetworkDirection.PLAY_TO_CLIENT);
    }

    private <T extends CoreCustomPacketPayload> void register(
            Class<T> type, CoreStreamCodec<FriendlyByteBuf, T> codec,
            BiConsumer<T, CorePacketContext> handler, NetworkDirection direction) {
        channel.registerMessage(nextMessageId++, type,
                (message, buffer) -> codec.encode(buffer, message),
                codec::decode,
                (message, supplier) -> {
                    var context = supplier.get();
                    try {
                        handler.accept(message, new CorePacketContext(context));
                    } finally {
                        context.setPacketHandled(true);
                    }
                }, Optional.of(direction));
    }

    public void sendToServer(CoreCustomPacketPayload message) {
        channel.sendToServer(message);
    }

    public void sendToPlayer(ServerPlayer player, CoreCustomPacketPayload message) {
        channel.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public void sendToAllPlayers(CoreCustomPacketPayload message) {
        channel.send(PacketDistributor.ALL.noArg(), message);
    }

    public void sendToDimension(ServerLevel level, CoreCustomPacketPayload message) {
        channel.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public void sendToPlayersTrackingEntityAndSelf(Entity entity, CoreCustomPacketPayload message) {
        channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunk, CoreCustomPacketPayload message) {
        channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunk(chunk.x, chunk.z)), message);
    }
}
