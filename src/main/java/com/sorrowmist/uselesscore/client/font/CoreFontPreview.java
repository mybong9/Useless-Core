package com.sorrowmist.uselesscore.client.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

/** A small independent atlas for pending settings; never double-draw a regular TTF to preview bold. */
public final class CoreFontPreview implements AutoCloseable {
    private static volatile List<GlyphProvider> vanilla=List.of();
    private static volatile long revision;
    public static void fallbacks(List<GlyphProvider> providers){vanilla=List.copyOf(providers);revision++;}
    private record Key(CoreFontSettings.Choice choice,String file,int size,boolean bold,String sample,long revision){}
    private Key key;
    private FontSet atlas;
    private Font font;
    private GlyphProvider owned;
    public Font font(CoreFontSettings.Options pending,String sample,Font fallback){
        var next=new Key(pending.choice(),pending.customFile(),pending.size(),pending.bold(),sample,revision);
        if(next.equals(key))return font==null?fallback:font;
        close();key=next;
        if(vanilla.isEmpty())return fallback;
        var points=new IntOpenHashSet(sample.codePoints().toArray());points.add(32);
        var providers=new ArrayList<GlyphProvider>();
        try {
            owned=CoreFontSettings.createProvider(pending);
            if(owned!=null)providers.add(subset(owned,points));
            for(var provider:vanilla)providers.add(new ScaledGlyphProvider(subset(provider,points),pending.size()/9f));
            atlas=new FontSet(Minecraft.getInstance().getTextureManager(),new ResourceLocation("useless_core","font_preview"));
            atlas.reload(providers);
            font=new Font(ignored->atlas,false);
            return font;
        }catch(java.io.IOException|RuntimeException failure){close();key=next;return fallback;}
    }
    private static GlyphProvider subset(GlyphProvider source,IntSet points){
        return new GlyphProvider(){
            public IntSet getSupportedGlyphs(){return points;}
            public GlyphInfo getGlyph(int point){return points.contains(point)?source.getGlyph(point):null;}
            // Resource-manager fallback providers are borrowed, not owned by this preview.
            public void close(){}
        };
    }
    @Override public void close(){
        if(atlas!=null){atlas.close();atlas=null;}
        if(owned!=null){owned.close();owned=null;}
        font=null;key=null;
    }
}
