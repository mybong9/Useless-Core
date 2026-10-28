package com.sorrowmist.uselesscore.mixin.client;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.sorrowmist.uselesscore.client.font.SmoothSheetGlyphInfo;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontTexture.class)
public abstract class CoreFontTextureMixin extends AbstractTexture {
    @Unique private boolean uselessCore$smooth;
    @Override public void setFilter(boolean blur,boolean mipmap){super.setFilter(blur || uselessCore$smooth,mipmap);}
    @Inject(method="add",at=@At("RETURN"))
    private void uselessCore$smoothAtlas(SheetGlyphInfo sheet,CallbackInfoReturnable<BakedGlyph> callback){
        if(callback.getReturnValue()==null)return;
        if(sheet instanceof SmoothSheetGlyphInfo)uselessCore$smooth=true;
        if(uselessCore$smooth)setFilter(true,false);
    }
}
