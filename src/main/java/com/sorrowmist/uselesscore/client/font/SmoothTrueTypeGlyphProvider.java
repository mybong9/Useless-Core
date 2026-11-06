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
    private final float baseline;
    public SmoothTrueTypeGlyphProvider(ByteBuffer memory,STBTTFontinfo info,int size,boolean bold) {
        super(memory,info,size,SAMPLES,0,0,"");
        nativeFont=info;rasterScale=STBTruetype.stbtt_ScaleForPixelHeight(info,size*SAMPLES);
        stroke=bold?Math.max(1,Math.round(SAMPLES*.45f*size/9f)):0;
        baseline=7f*size/9f;
    }
    @Override public GlyphInfo getGlyph(int point) {
        GlyphInfo glyph=super.getGlyph(point);if(glyph==null)return null;
        int[] x0={0},y0={0},x1={0},y1={0};
        STBTruetype.stbtt_GetCodepointBitmapBox(nativeFont,point,rasterScale,rasterScale,x0,y0,x1,y1);
        return new GlyphInfo() {
            public float getAdvance(){return glyph.getAdvance()+stroke/(float)SAMPLES;}
            public float getBoldOffset(){return stroke>0?0:glyph.getBoldOffset();}
            public float getShadowOffset(){return glyph.getShadowOffset();}
            public BakedGlyph bake(Function<SheetGlyphInfo,BakedGlyph> stitch) {
                return glyph.bake(sheet->stitch.apply(new SmoothSheetGlyphInfo() {
                    public int getPixelWidth(){return sheet.getPixelWidth()+stroke+2;}
                    public int getPixelHeight(){return sheet.getPixelHeight()+2;}
                    public boolean isColored(){return false;}
                    public float getOversample(){return SAMPLES;}
                    // Use the raster's actual bounds and a shared baseline, not font-dependent ascent/bearings.
                    public float getLeft(){return (x0[0]-1f)/SAMPLES;}
                    public float getRight(){return getLeft()+getPixelWidth()/(float)SAMPLES;}
                    public float getUp(){return 3f+baseline+(y0[0]-1f)/SAMPLES;}
                    public float getDown(){return getUp()+getPixelHeight()/(float)SAMPLES;}
                    public void upload(int x,int y) {
                        int w=sheet.getPixelWidth(),h=sheet.getPixelHeight();
                        ByteBuffer coverage=MemoryUtil.memAlloc(w*h);
                        try(NativeImage image=new NativeImage(NativeImage.Format.LUMINANCE,w+stroke+2,h+2,false)) {
                            STBTruetype.stbtt_MakeCodepointBitmap(nativeFont,coverage,w,h,w,rasterScale,rasterScale,point);
                            for(int row=-1;row<=h;row++)for(int col=-1;col<=w+stroke;col++) {
                                int alpha=0;
                                if(row>=0 && row<h)for(int s=0;s<=stroke;s++){int source=col-s;if(source>=0 && source<w)alpha=Math.max(alpha,coverage.get(row*w+source)&255);}
                                image.setPixelLuminance(col+1,row+1,(byte)alpha);
                            }
                            image.upload(0,x,y,false);
                        }finally{MemoryUtil.memFree(coverage);}
                    }
                }));
            }
        };
    }
}
