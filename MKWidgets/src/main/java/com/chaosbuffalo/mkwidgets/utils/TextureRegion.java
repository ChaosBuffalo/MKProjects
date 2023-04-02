package com.chaosbuffalo.mkwidgets.utils;

public class TextureRegion {
    public final String regionName;
    public final int u;
    public final int v;
    public final int width;
    public final int height;

    public TextureRegion(String regionName, int u, int v, int width, int height){
        this.regionName = regionName;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }
}
