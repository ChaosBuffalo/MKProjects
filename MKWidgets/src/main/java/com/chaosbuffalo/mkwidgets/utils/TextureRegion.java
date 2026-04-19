package com.chaosbuffalo.mkwidgets.utils;

/**
 * Immutable description of a rectangular region inside a texture atlas.
 */
public class TextureRegion {
    public final String regionName;
    public final int u;
    public final int v;
    public final int width;
    public final int height;

    /**
     * @param regionName region lookup name
     * @param u source u coordinate
     * @param v source v coordinate
     * @param width region width
     * @param height region height
     */
    public TextureRegion(String regionName, int u, int v, int width, int height) {
        this.regionName = regionName;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }
}
