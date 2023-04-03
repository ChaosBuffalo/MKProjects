package com.chaosbuffalo.mkwidgets.client.gui.math;

public class IntColor {
    private int colorInt;
    private int a;
    private int r;
    private int g;
    private int b;

    public IntColor(int color) {
        a = color >> 24 & 255;
        r = color >> 16 & 255;
        g = color >> 8 & 255;
        b = color & 255;
        colorInt = color;
    }

    public IntColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.colorInt = a << 24 + r << 16 + b << 8 + g;
    }

    public int getAsInt() {
        return colorInt;
    }

    public int getAlpha() {
        return a;
    }

    public int getRed() {
        return r;
    }

    public int getBlue() {
        return b;
    }

    public int getGreen() {
        return g;
    }

    public float getAlphaF() {
        return (float) a / 255.0f;
    }

    public float getRedF() {
        return (float) r / 255.0f;
    }

    public float getBlueF() {
        return (float) b / 255.0f;
    }

    public float getGreenF() {
        return (float) g / 255.0f;
    }

}
