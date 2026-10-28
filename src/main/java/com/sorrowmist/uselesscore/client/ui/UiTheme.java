package com.sorrowmist.uselesscore.client.ui;

/** Client-only colour tokens shared by glass machine interfaces. */
public record UiTheme(int panel, int surface, int inset, int border, int text, int muted,
                      int accent, int selected, int danger) {
    public static final UiTheme LIGHT = new UiTheme(0xf4e9ebef, 0xfff6f7f9, 0xffe0e4e9,
            0xffbfc6d0, 0xff242d3b, 0xff687484, 0xff1467e8, 0xffdcecff, 0xffbc525b);
    public static final UiTheme DARK = new UiTheme(0xb3353e49, 0x426f7982, 0x30202a33,
            0x709caab9, 0xffedf3fa, 0xffbbc7d3, 0xff3299ff, 0x70417eac, 0xffff9297);
}
