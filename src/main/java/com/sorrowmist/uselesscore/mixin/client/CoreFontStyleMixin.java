package com.sorrowmist.uselesscore.mixin.client;

import com.sorrowmist.uselesscore.client.font.CoreFontSettings;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

@Mixin(targets="net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class CoreFontStyleMixin {
    @WrapOperation(method="accept",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/Font;renderChar(Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;ZZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFI)V"))
    private void uselessCore$singleBoldGlyph(Font font,BakedGlyph glyph,boolean bold,boolean italic,
            float offset,float x,float y,Matrix4f pose,VertexConsumer vertices,
            float red,float green,float blue,float alpha,int light,Operation<Void> original){
        original.call(font,glyph,bold && offset!=0f,italic,offset,x,y,pose,vertices,red,green,blue,alpha,light);
    }
    @Shadow @Final private float r;
    @Shadow @Final private float g;
    @Shadow @Final private float b;
    @ModifyVariable(method="accept",at=@At("HEAD"),argsOnly=true)
    private Style uselessCore$style(Style original){
        // Preserve warning, rarity and status colors also supplied directly by renderers.
        boolean neutral=Math.abs(r-g)<.001f && Math.abs(g-b)<.001f;
        return CoreFontSettings.style(original,neutral);
    }
}
