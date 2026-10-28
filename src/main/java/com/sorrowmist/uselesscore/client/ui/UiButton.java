package com.sorrowmist.uselesscore.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Vanilla focus, activation and narration with animated glass surfaces. */
public class UiButton extends Button {
    private final UiTheme theme;
    private boolean selected,primary,destructive,circle,ghost;
    private final UiMotion hover=new UiMotion(0),selection=new UiMotion(0),press=new UiMotion(0);
    public UiButton(int x,int y,int w,int h,Component text,OnPress action){this(x,y,w,h,text,action,UiTheme.DARK);}
    public UiButton(int x,int y,int w,int h,Component text,OnPress action,UiTheme theme){
        super(x,y,w,h,text,action,DEFAULT_NARRATION);this.theme=theme;
    }
    public UiButton primary(){primary=true;return this;}
    public UiButton destructive(){destructive=true;return this;}
    public UiButton circle(){circle=true;return this;}
    public UiButton ghost(){ghost=true;return this;}
    public void selected(boolean value){selected=value;}
    public void releaseVisualState(){}
    @Override public void onClick(double x,double y){press.snap(1);super.onClick(x,y);}
    @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial){
        float over=hover.to(active&&isHoveredOrFocused()?1:0),chosen=selection.to(selected?1:0),pressed=press.to(0);
        int fill=UiDraw.mix(theme.surface(),theme.selected(),over*.3f+chosen*.7f);
        int edge=UiDraw.mix(theme.border(),theme.accent(),over*.45f+chosen*.55f);
        int top=fill,bottom=UiDraw.mix(fill,0xff000000,.06f),shadow=0x16000000;
        float softness=1.6f;
        if(primary&&active){top=0xff339dff;bottom=0xff0864e9;edge=0xa0b9ddff;shadow=0x603291fc;softness=3;}
        if(destructive&&active){top=0xeaff989c;bottom=0xe9e45b67;edge=0x88ffd7da;shadow=0x28e75460;}
        if(!active){top=UiDraw.alpha(top,.45f);bottom=UiDraw.alpha(bottom,.45f);edge=UiDraw.alpha(edge,.5f);shadow=0;}
        float radius=circle?Math.min(width,height)*.5f:Math.min(10,height*.5f);
        if(circle){
            top=UiDraw.mix(UiDraw.alpha(theme.surface(),over*.5f),0xff37a4ff,chosen);
            bottom=UiDraw.mix(UiDraw.alpha(theme.surface(),over*.5f),0xff0969ef,chosen);
            edge=UiDraw.alpha(0xff91ceff,chosen*.8f);shadow=UiDraw.alpha(0x9070beff,chosen);softness=3.5f;
        }else if(ghost){top=UiDraw.alpha(fill,over*.5f);bottom=top;edge=0;shadow=0;}
        g.pose().pushPose();
        float scale=1-.04f*pressed;
        g.pose().translate(getX()+width*.5f,getY()+height*.5f,0);g.pose().scale(scale,scale,1);
        g.pose().translate(-getX()-width*.5f,-getY()-height*.5f,0);
        UiDraw.gradient(g,getX(),getY(),width,height,radius,top,bottom,edge,.65f,shadow,softness);
        int colour=!active?theme.muted():(primary||destructive||selected)?0xfff4f8ff:theme.text();
        renderScrollingString(g,Minecraft.getInstance().font,3,colour);
        g.pose().popPose();
    }
}
