package com.sorrowmist.uselesscore.mixin;

import com.sorrowmist.uselesscore.api.event.CoreBlockDropsEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockDropsMixin {
    @Redirect(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V", remap = false,
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static List<ItemStack> useless$coreDrops(BlockState state, ServerLevel level, BlockPos pos,
            BlockEntity blockEntity, Entity breaker, ItemStack tool) {
        return CoreBlockDropsEvent.post(Block.getDrops(state, level, pos, blockEntity, breaker, tool),
                level, pos, state, blockEntity, breaker, tool);
    }
}
