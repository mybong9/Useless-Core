package com.sorrowmist.uselesscore.client.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import java.util.function.Function;

/** Scale geometry and advances together, leaving the source atlas and fallback coverage intact. */
public record ScaledGlyphProvider(GlyphProvider source,float scale) implements GlyphProvider {
    @Override public IntSet getSupportedGlyphs(){return source.getSupportedGlyphs();}
    @Override public void close(){source.close();}
    @Override public GlyphInfo getGlyph(int point) {
        GlyphInfo glyph=source.getGlyph(point);if(glyph==null)return null;
        return new GlyphInfo() {
            public float getAdvance(){return glyph.getAdvance()*scale;}
            public float getBoldOffset(){return glyph.getBoldOffset()*scale;}
            public float getShadowOffset(){return glyph.getShadowOffset()*scale;}
            public BakedGlyph bake(Function<SheetGlyphInfo,BakedGlyph> stitch){
                return glyph.bake(sheet->stitch.apply(new SheetGlyphInfo() {
                    public int getPixelWidth(){return sheet.getPixelWidth();}
                    public int getPixelHeight(){return sheet.getPixelHeight();}
                    public boolean isColored(){return sheet.isColored();}
                    public float getOversample(){return sheet.getOversample()/scale;}
                    public void upload(int x,int y){sheet.upload(x,y);}
                    public float getLeft(){return sheet.getLeft()*scale;}
                    public float getRight(){return sheet.getRight()*scale;}
                    public float getUp(){return sheet.getUp()*scale;}
                    public float getDown(){return sheet.getDown()*scale;}
                }));
            }
        };
    }
}
