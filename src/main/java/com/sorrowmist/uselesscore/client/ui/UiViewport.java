package com.sorrowmist.uselesscore.client.ui;

/** Pure coordinate contract: fit a fixed logical menu without changing Minecraft's GUI scale. */
public record UiViewport(int width, int height, double scale) {
    public static UiViewport fit(int width, int height, int panelWidth, int panelHeight) {
        return fit(width,height,panelWidth,panelHeight,1);
    }
    public static UiViewport fit(int width,int height,int panelWidth,int panelHeight,double preferred) {
        return new UiViewport(width, height, Math.max(.01,
                Math.min(preferred, Math.min((width - 12.0) / panelWidth, (height - 12.0) / panelHeight))));
    }
    public int canvasWidth() { return (int) Math.ceil(width / scale); }
    public int canvasHeight() { return (int) Math.ceil(height / scale); }
    public double local(double physical) { return physical / scale; }
    public int physical(double logical) { return (int) Math.floor(logical * scale); }
}
