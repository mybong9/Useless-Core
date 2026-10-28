package com.sorrowmist.uselesscore.mixin.client;

import com.sorrowmist.uselesscore.client.font.CoreFontSettings;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public abstract class CoreFontWidthMixin {
    @ModifyVariable(method={
            "drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I",
            "drawInternal(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I"
    },at=@At("HEAD"),argsOnly=true,ordinal=0)
    private boolean uselessCore$shadow(boolean requested){return CoreFontSettings.shadow(requested);}
    @ModifyArg(method="<init>",at=@At(value="INVOKE",target="Lnet/minecraft/client/StringSplitter;<init>(Lnet/minecraft/client/StringSplitter$WidthProvider;)V"),index=0)
    private StringSplitter.WidthProvider uselessCore$width(StringSplitter.WidthProvider original){
        return (point,style)->original.getWidth(point,CoreFontSettings.style(style));
    }
}
