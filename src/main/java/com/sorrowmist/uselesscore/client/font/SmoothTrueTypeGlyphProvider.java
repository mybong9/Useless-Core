package com.sorrowmist.uselesscore.client.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import java.util.function.Function;

/** Rasterize at the chosen size; bold is one coverage mask, never two offset glyph draws. */
public final class SmoothTrueTypeGlyphProvider extends TrueTypeGlyphProvider {
    private static final int SAMPLES=3;
    private final STBTTFontinfo nativeFont;
    private final float rasterScale;
    private final int stroke;
    public SmoothTrueTypeGlyphProvider(ByteBuffer memory,STBTTFontinfo info,int size,boolean bold) {
        super(memory,info,size,SAMPLES,0,0,"");
        nativeFont=info;rasterScale=STBTruetype.stbtt_ScaleForPixelHeight(info,size*SAMPLES);
        stroke=bold?Math.max(1,Math.round(SAMPLES*.45f*size/9f)):0;
    }
    @Override public GlyphInfo getGlyph(int point) {
        GlyphInfo glyph=super.getGlyph(point);if(glyph==null)return null;
        return new GlyphInfo() {
            public float getAdvance(){return glyph.getAdvance()+stroke/(float)SAMPLES;}
            public float getBoldOffset(){return stroke>0?0:glyph.getBoldOffset();}
            public float getShadowOffset(){return glyph.getShadowOffset();}
            public BakedGlyph bake(Function<SheetGlyphInfo,BakedGlyph> stitch) {
                return glyph.bake(sheet->stitch.apply(new SmoothSheetGlyphInfo() {
                    public int getPixelWidth(){return sheet.getPixelWidth()+stroke+2;}
                    public int getPixelHeight(){return sheet.getPixelHeight()+2;}
                    public boolean isColored(){return true;}
                    public float getOversample(){return SAMPLES;}
                    public float getLeft(){return sheet.getLeft()-1f/SAMPLES;}
                    public float getRight(){return sheet.getRight()+(stroke+1f)/SAMPLES;}
                    public float getUp(){return sheet.getUp()-1f/SAMPLES;}
                    public float getDown(){return sheet.getDown()+1f/SAMPLES;}
                    public void upload(int x,int y) {
                        int w=sheet.getPixelWidth(),h=sheet.getPixelHeight();
                        ByteBuffer coverage=MemoryUtil.memAlloc(w*h);
                        try(NativeImage image=new NativeImage(w+stroke+2,h+2,false)) {
                            STBTruetype.stbtt_MakeCodepointBitmap(nativeFont,coverage,w,h,w,rasterScale,rasterScale,point);
                            for(int row=-1;row<=h;row++)for(int col=-1;col<=w+stroke;col++) {
                                int alpha=0;
                                if(row>=0 && row<h)for(int s=0;s<=stroke;s++){int source=col-s;if(source>=0 && source<w)alpha=Math.max(alpha,coverage.get(row*w+source)&255);}
                                image.setPixelRGBA(col+1,row+1,(alpha<<24)|0xffffff);
                            }
                            image.upload(0,x,y,false);
                        }finally{MemoryUtil.memFree(coverage);}
                    }
                }));
            }
        };
    }
}
