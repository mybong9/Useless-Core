package com.sorrowmist.uselesscore.mixin.client;

import com.mojang.blaze3d.font.GlyphProvider;
import com.sorrowmist.uselesscore.client.font.CoreFontSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.ArrayList;
import java.util.List;

@Mixin(FontSet.class)
public abstract class CoreFontSetMixin {
    @Shadow @Final private ResourceLocation name;
    @ModifyVariable(method="reload",at=@At("HEAD"),argsOnly=true)
    private List<GlyphProvider> uselessCore$font(List<GlyphProvider> original) {
        if(!Minecraft.DEFAULT_FONT.equals(name) && !Minecraft.UNIFORM_FONT.equals(name))return original;
        var custom=CoreFontSettings.createProvider();
        float scale=CoreFontSettings.scale();
        if(custom==null && scale==1)return original;
        var providers=new ArrayList<GlyphProvider>(original.size()+1);
        if(custom!=null)providers.add(custom);
        for(var provider:original)providers.add(scale==1?provider:new com.sorrowmist.uselesscore.client.font.ScaledGlyphProvider(provider,scale));
        return providers;
    }
}
