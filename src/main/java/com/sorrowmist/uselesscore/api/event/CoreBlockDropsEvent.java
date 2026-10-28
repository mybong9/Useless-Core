package com.sorrowmist.uselesscore.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import java.util.ArrayList;
import java.util.List;

/** Mutable item drops before Block.dropResources spawns them; cancellation suppresses items. */
@Cancelable
public final class CoreBlockDropsEvent extends BlockEvent {
    private final ServerLevel level;
    private final Entity breaker;
    private final ItemStack tool;
    private final BlockEntity blockEntity;
    private final List<ItemEntity> drops;

    private CoreBlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state,
            BlockEntity blockEntity, Entity breaker, ItemStack tool, List<ItemEntity> drops) {
        super(level, pos, state);
        this.level = level; this.blockEntity = blockEntity;
        this.breaker = breaker; this.tool = tool; this.drops = drops;
    }
    @Override public ServerLevel getLevel() { return level; }
    public Entity getBreaker() { return breaker; }
    public ItemStack getTool() { return tool; }
    public BlockEntity getBlockEntity() { return blockEntity; }
    public List<ItemEntity> getDrops() { return drops; }

    public static List<ItemStack> post(List<ItemStack> stacks, ServerLevel level, BlockPos pos,
            BlockState state, BlockEntity blockEntity, Entity breaker, ItemStack tool) {
        List<ItemEntity> entities = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) entities.add(new ItemEntity(level,
                    pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack.copy()));
        }
        var event = new CoreBlockDropsEvent(level, pos, state, blockEntity, breaker, tool, entities);
        if (MinecraftForge.EVENT_BUS.post(event)) return List.of();
        return event.drops.stream().map(ItemEntity::getItem).filter(s -> !s.isEmpty()).toList();
    }
}
