package com.sorrowmist.uselesscore.mixin.client;

import com.sorrowmist.uselesscore.client.ui.FittedContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tooltips constrain to the logical canvas only during its own rendering. */
@Mixin(GuiGraphics.class)
public abstract class FittedTooltipMixin {
    @Redirect(method="enableScissor",at=@At(value="NEW",target="net/minecraft/client/gui/navigation/ScreenRectangle"))
    private ScreenRectangle uselessCore$scaledClip(int x,int y,int width,int height) {
        var screen=FittedContainerScreen.drawingCanvas();
        if(screen==null)return new ScreenRectangle(x,y,width,height);
        var rect=screen.screenRect(x,y,width,height);
        return new ScreenRectangle(rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight());
    }
    @Inject(method="guiWidth",at=@At("HEAD"),cancellable=true)
    private void uselessCore$tooltipWidth(CallbackInfoReturnable<Integer> cir) {
        var screen=FittedContainerScreen.drawingCanvas();
        if(screen!=null)cir.setReturnValue(screen.canvasWidth());
    }
    @Inject(method="guiHeight",at=@At("HEAD"),cancellable=true)
    private void uselessCore$tooltipHeight(CallbackInfoReturnable<Integer> cir) {
        var screen=FittedContainerScreen.drawingCanvas();
        if(screen!=null)cir.setReturnValue(screen.canvasHeight());
    }
}
