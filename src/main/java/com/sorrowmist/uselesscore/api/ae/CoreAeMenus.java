package com.sorrowmist.uselesscore.api.ae;

import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.MenuTypeBuilder;
import com.sorrowmist.uselesscore.mixin.MenuTypeBuilderAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;

/** Builds an AE menu that its owning mod can register through DeferredRegister. */
public final class CoreAeMenus {
    private CoreAeMenus() {}
    @SuppressWarnings("unchecked")
    public static <M extends AEBaseMenu, H> MenuType<M> buildUnregistered(
            MenuTypeBuilder<M, H> builder, ResourceLocation id) {
        MenuTypeBuilderAccess access = (MenuTypeBuilderAccess) (Object) builder;
        access.core$setId(id);
        MenuType<M> type = IForgeMenuType.create((syncId, inventory, buffer) ->
                (M) access.core$fromNetwork(syncId, inventory, buffer));
        access.core$setMenuType(type);
        MenuOpener.addOpener(type, access::core$open);
        return type;
    }
}
