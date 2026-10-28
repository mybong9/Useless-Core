package com.sorrowmist.uselesscore.api.ae;
import com.sorrowmist.uselesscore.api.component.CoreItemComponents;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import com.sorrowmist.uselesscore.api.component.UselessComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;


/** Player-scoped access to the AE network bound to a tool. */
public final class CoreLinkedGrid {
    private CoreLinkedGrid() {}
    public record Access(IGrid grid, IActionSource source) {}
    @Nullable public static Access resolve(ItemStack tool, Player player) {
        GlobalPos linkedPos = CoreItemComponents.get(tool, UselessComponents.WIRELESS_LINK_TARGET.get());
        MinecraftServer server = player.getServer();
        if (linkedPos == null || server == null) return null;
        ServerLevel targetLevel = server.getLevel(linkedPos.dimension());
        if (targetLevel == null || !targetLevel.isLoaded(linkedPos.pos())) return null;
        if (!(targetLevel.getBlockEntity(linkedPos.pos()) instanceof WirelessAccessPointBlockEntity accessPoint)
                || !accessPoint.getMainNode().isOnline()) return null;
        IGrid grid = accessPoint.getGrid();
        return grid == null ? null : new Access(grid, IActionSource.ofPlayer(player));
    }
    public static long extract(ItemStack tool, Player player, ItemStack stack, Actionable mode) {
        Access access=resolve(tool,player); AEItemKey key=AEItemKey.of(stack);
        return access==null || key==null || stack.isEmpty() ? 0 : access.grid().getStorageService().getInventory().extract(key,stack.getCount(),mode,access.source());
    }
    public static long insert(ItemStack tool, Player player, ItemStack stack, Actionable mode) {
        Access access=resolve(tool,player); AEItemKey key=AEItemKey.of(stack);
        return access==null || key==null || stack.isEmpty() ? 0 : access.grid().getStorageService().getInventory().insert(key,stack.getCount(),mode,access.source());
    }
}
