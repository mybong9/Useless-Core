package com.sorrowmist.uselesscore.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sorrowmist.uselesscore.client.ui.FittedContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Recipe browser overlays must not inherit a fitted menu's local transform. */
@Mixin(AbstractContainerScreen.class)
public abstract class FittedContainerEventsMixin {
    @WrapOperation(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z", remap = false))
    private boolean uselessCore$physicalOverlay(IEventBus bus, Event event, Operation<Boolean> original) {
        if ((Object) this instanceof FittedContainerScreen<?> fitted && event instanceof ContainerScreenEvent.Render render)
            return fitted.dispatchUnscaled(render, next -> original.call(bus, next));
        return original.call(bus, event);
    }
}
