package com.sorrowmist.uselesscore.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Keeps vanilla selection, clipboard, cursor, input and narration. */
public final class UiEditBox extends EditBox {
    private final UiTheme theme;
    private boolean plain;
    private final UiMotion focus=new UiMotion(0);
    public UiEditBox(Font font,int x,int y,int w,int h,Component label){this(font,x,y,w,h,label,UiTheme.DARK);}
    public UiEditBox(Font font,int x,int y,int w,int h,Component label,UiTheme theme){
        super(font,x,y,w,h,label);this.theme=theme;
        setTextColor(theme.text());setTextColorUneditable(theme.muted());
    }
    public UiEditBox plain(){plain=true;return this;}
    @Override public void renderWidget(GuiGraphics g,int mx,int my,float partial){
        if(!visible)return;
        float amount=focus.to(isFocused()?1:0);
        if(!plain||amount>.01f)UiDraw.gradient(g,getX(),getY(),width,height,Math.min(10,height*.5f),
                plain?0:theme.surface(),plain?0:theme.inset(),
                UiDraw.mix(plain?0:theme.border(),theme.accent(),amount),.6f,UiDraw.alpha(0x503a9fff,amount),2);
        int x=getX(),y=getY(),w=width,h=height;
        setBordered(false);setX(x+4);setY(y+(h-8)/2);width=Math.max(1,w-8);
        try{super.renderWidget(g,mx,my,partial);}
        finally{setX(x);setY(y);width=w;setBordered(true);}
    }
}
