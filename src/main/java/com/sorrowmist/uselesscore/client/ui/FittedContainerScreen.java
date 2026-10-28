package com.sorrowmist.uselesscore.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.eventbus.api.Event;
import java.util.function.Function;

/**
 * Fixed-layout menus fitted to the actual GUI viewport. All vanilla slot interaction remains in
 * logical coordinates. Recipe-browser targets and Forge render events stay in screen coordinates.
 * Subclasses override *Content hooks, never scale individual slots or reinterpret packet indices.
 */
public abstract class FittedContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private UiViewport viewport = new UiViewport(1, 1, 1);
    private boolean resizing;
    private double resizeX,resizeY,resizeScale;
    private long lastResize;
    private static FittedContainerScreen<?> drawing;
    /** Non-null only while our local canvas is being drawn, never during external overlays. */
    public static FittedContainerScreen<?> drawingCanvas() { return drawing; }
    protected FittedContainerScreen(T menu, Inventory inventory, Component title) { super(menu, inventory, title); }
    @Override protected void init() {
        viewport = UiViewport.fit(width,height,imageWidth,imageHeight,UiWindowSettings.get(getClass().getName()));
        width = canvasWidth(); height = canvasHeight();
        super.init();
        width = viewport.width(); height = viewport.height();
    }
    public final int canvasWidth() { return viewport.canvasWidth(); }
    public final int canvasHeight() { return viewport.canvasHeight(); }
    public final Rect2i screenRect(int x, int y, int w, int h) {
        int sx = viewport.physical(x), sy = viewport.physical(y);
        return new Rect2i(sx, sy, (int) Math.ceil((x + w) * viewport.scale()) - sx,
                (int) Math.ceil((y + h) * viewport.scale()) - sy);
    }
    @Override public int getGuiLeft() { return viewport.physical(leftPos); }
    @Override public int getGuiTop() { return viewport.physical(topPos); }
    @Override public int getXSize() { return screenRect(leftPos, topPos, imageWidth, imageHeight).getWidth(); }
    @Override public int getYSize() { return screenRect(leftPos, topPos, imageWidth, imageHeight).getHeight(); }
    @Override public final void render(GuiGraphics g, int mx, int my, float partial) {
        UiRender.captureScene(g);
        FittedContainerScreen<?> previous = drawing; drawing = this;
        g.flush(); g.pose().pushPose(); g.pose().scale((float) viewport.scale(), (float) viewport.scale(), 1);
        width = canvasWidth(); height = canvasHeight();
        try {
            int lx=(int)viewport.local(mx),ly=(int)viewport.local(my);
            renderContent(g,lx,ly,partial);
            g.pose().pushPose();g.pose().translate(0,0,850);
            int right=leftPos+imageWidth-5,bottom=topPos+imageHeight-5;
            UiDraw.line(g,right-6,bottom,right,bottom-6,.8f,0xff97afc3);
            UiDraw.line(g,right-3,bottom,right,bottom-3,.8f,0xff97afc3);
            if(resizing||overGrip(mx,my))g.renderTooltip(font,Component.translatable("gui.useless_core.ui.resize",Math.round(viewport.scale()*100)),lx,ly);
            g.pose().popPose();g.flush();
        }
        finally { width = viewport.width(); height = viewport.height(); g.pose().popPose(); drawing = previous; }
    }
    protected void renderContent(GuiGraphics g, int mx, int my, float partial) { super.render(g, mx, my, partial); }
    @Override public void removed() { super.removed(); UiRender.releaseScene(); }
    private boolean overGrip(double x,double y){int r=getGuiLeft()+getXSize(),b=getGuiTop()+getYSize();return x>=r-12&&x<=r+3&&y>=b-12&&y<=b+3;}
    public final void setWindowScale(double scale){UiWindowSettings.set(getClass().getName(),scale);rebuildWidgets();UiWindowSettings.save();}
    @Override public final boolean mouseClicked(double x, double y, int b) {
        if(overGrip(x,y)){
            if(b==1){setWindowScale(1);return true;}
            if(b==0){resizing=true;resizeX=x;resizeY=y;resizeScale=viewport.scale();return true;}
        }
        return clickContent(viewport.local(x), viewport.local(y), b);
    }
    protected boolean clickContent(double x, double y, int b) { return super.mouseClicked(x, y, b); }
    @Override public final boolean mouseReleased(double x, double y, int b) {if(resizing){resizing=false;UiWindowSettings.save();return true;}return releaseContent(viewport.local(x), viewport.local(y), b); }
    protected boolean releaseContent(double x, double y, int b) { return super.mouseReleased(x, y, b); }
    @Override public final boolean mouseDragged(double x, double y, int b, double dx, double dy) {
        if(resizing){
            long now=System.nanoTime();if(now-lastResize>40_000_000){
                double sx=2*(x-resizeX)/imageWidth,sy=2*(y-resizeY)/imageHeight;
                double limit=Math.min((viewport.width()-12d)/imageWidth,(viewport.height()-12d)/imageHeight);
                UiWindowSettings.set(getClass().getName(),Math.min(limit,resizeScale+(Math.abs(sx)>Math.abs(sy)?sx:sy)));
                rebuildWidgets();lastResize=now;
            }return true;
        }
        return dragContent(viewport.local(x), viewport.local(y), b, viewport.local(dx), viewport.local(dy));
    }
    protected boolean dragContent(double x, double y, int b, double dx, double dy) { return super.mouseDragged(x, y, b, dx, dy); }
    @Override public final boolean mouseScrolled(double x, double y, double delta) {
        if(hasControlDown()){setWindowScale(viewport.scale()+Math.signum(delta)*.05);return true;}
        return scrollContent(viewport.local(x), viewport.local(y), delta);
    }
    protected boolean scrollContent(double x, double y, double delta) { return super.mouseScrolled(x, y, delta); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(viewport.local(x), viewport.local(y)); }

    /** Called only around Forge's two container render events by the client mixin. */
    public final boolean dispatchUnscaled(ContainerScreenEvent.Render event, Function<Event, Boolean> post) {
        GuiGraphics g = event.getGuiGraphics();
        int oldWidth = width, oldHeight = height;
        FittedContainerScreen<?> previous = drawing; drawing = null;
        boolean foreground = event instanceof ContainerScreenEvent.Render.Foreground;
        int mx = viewport.physical(event.getMouseX()), my = viewport.physical(event.getMouseY());
        g.flush(); g.pose().pushPose();
        g.pose().last().pose().identity(); g.pose().last().normal().identity();
        if (foreground) g.pose().translate(getGuiLeft(), getGuiTop(), 0);
        width = viewport.width(); height = viewport.height();
        try {
            return post.apply(foreground ? new ContainerScreenEvent.Render.Foreground(this, g, mx, my)
                    : new ContainerScreenEvent.Render.Background(this, g, mx, my));
        } finally { g.flush(); g.pose().popPose(); width = oldWidth; height = oldHeight; drawing = previous; }
    }
}
