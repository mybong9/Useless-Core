package com.sorrowmist.uselesscore.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Shared vector primitives; edges are evaluated at framebuffer pixel resolution. */
public final class UiDraw {
    private UiDraw() {}
    public static void rounded(GuiGraphics g, int x, int y, int w, int h, int radius, int colour) {
        gradient(g,x,y,w,h,radius,colour,colour,0,0,0,0);
    }
    public static void gradient(GuiGraphics g,float x,float y,float w,float h,float radius,
                                int top,int bottom,int edge,float border,int shadow,float softness) {
        if(UiRender.shape(g,x,y,w,h,radius,top,bottom,edge,border,shadow,softness,false))return;
        fallback(g,(int)x,(int)y,(int)w,(int)h,(int)radius,top);
    }
    public static void glass(GuiGraphics g,int x,int y,int w,int h,int radius,UiTheme theme) {
        int top=theme==UiTheme.LIGHT?0xb3edf0f5:0x963a424e;
        int bottom=theme==UiTheme.LIGHT?0xb9e0e6ee:0x9c242c35;
        if(!UiRender.shape(g,x,y,w,h,radius,top,bottom,theme.border(),.65f,0x60060c16,5,true))
            panel(g,x,y,w,h,radius,theme.panel(),theme.border());
    }
    public static void panel(GuiGraphics g,int x,int y,int w,int h,int radius,int fill,int edge) {
        gradient(g,x,y,w,h,radius,fill,mix(fill,0xff000000,.045f),edge,.6f,0x18000000,1.2f);
    }
    public static void slot(GuiGraphics g,int x,int y,int size,UiTheme theme,boolean selected) {
        panel(g,x,y,size,size,4,selected?theme.selected():theme.inset(),selected?theme.accent():theme.border());
    }
    public static void check(GuiGraphics g,int x,int y,int size,float selected) {
        gradient(g,x,y,size,size,size*.5f,mix(0x153e5466,0xff319fff,selected),
                mix(0x104a5c68,0xff0068ed,selected),mix(0x669daab5,0xff7dc8ff,selected),.7f,
                alpha(0x8051afff,selected),selected*2.5f);
        if(selected>.02f){
            line(g,x+size*.25f,y+size*.52f,x+size*.43f,y+size*.7f,.85f,alpha(0xfff3faff,selected));
            line(g,x+size*.43f,y+size*.7f,x+size*.76f,y+size*.32f,.85f,alpha(0xfff3faff,selected));
        }
    }
    public static void line(GuiGraphics g,float x1,float y1,float x2,float y2,float thickness,int colour) {
        float len=(float)Math.hypot(x2-x1,y2-y1);
        g.pose().pushPose();g.pose().translate(x1,y1,0);
        g.pose().mulPose(com.mojang.math.Axis.ZP.rotation((float)Math.atan2(y2-y1,x2-x1)));
        gradient(g,0,-thickness*.5f,len,thickness,thickness*.5f,colour,colour,0,0,0,0);
        g.pose().popPose();
    }
    public static void text(GuiGraphics g,Font font,String value,int x,int y,int maxWidth,int colour) {
        String clipped=font.width(value)<=maxWidth?value:font.plainSubstrByWidth(value,Math.max(0,maxWidth-font.width("…")))+"…";
        g.drawString(font,clipped,x,y,colour,false);
    }
    public static int alpha(int argb,float opacity){return (Math.round((argb>>>24)*Math.max(0,Math.min(1,opacity)))<<24)|(argb&0xffffff);}
    public static int mix(int a,int b,float t){
        t=Math.max(0,Math.min(1,t));int out=0;
        for(int shift=0;shift<=24;shift+=8)out|=Math.round(((a>>>shift)&255)*(1-t)+((b>>>shift)&255)*t)<<shift;
        return out;
    }
    private static void fallback(GuiGraphics g,int x,int y,int w,int h,int radius,int colour){
        if(w<=0||h<=0)return;int r=Math.max(0,Math.min(radius,Math.min(w,h)/2));
        g.fill(x,y+r,x+w,y+h-r,colour);
        for(int row=0;row<r;row++){double dy=r-row-.5;int inset=(int)Math.ceil(r-Math.sqrt(r*r-dy*dy));
            g.fill(x+inset,y+row,x+w-inset,y+row+1,colour);g.fill(x+inset,y+h-row-1,x+w-inset,y+h-row,colour);}
    }
}
