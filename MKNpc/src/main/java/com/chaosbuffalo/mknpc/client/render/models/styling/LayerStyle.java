package com.chaosbuffalo.mknpc.client.render.models.styling;

public class LayerStyle {
    public enum RenderMode {
        CUTOUT,
        TRANSLUCENT,
        ENERGY_SWIRL
    }

    private final float layerSize;
    private final String layerName;
    private final RenderMode renderMode;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final float scrollU;
    private final float scrollV;
    private final boolean fullBright;

    public float getLayerSize() {
        return layerSize;
    }

    public String getLayerName() {
        return layerName;
    }

    public boolean isTranslucent() {
        return renderMode == RenderMode.TRANSLUCENT;
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public float getScrollU() {
        return scrollU;
    }

    public float getScrollV() {
        return scrollV;
    }

    public boolean isFullBright() {
        return fullBright;
    }

    public LayerStyle(String layerName, float size) {
        this(layerName, size, RenderMode.CUTOUT);
    }

    public LayerStyle(String layerName, float size, boolean isTranslucent) {
        this(layerName, size, isTranslucent ? RenderMode.TRANSLUCENT : RenderMode.CUTOUT);
    }

    public LayerStyle(String layerName, float size, RenderMode renderMode) {
        this(layerName, size, renderMode, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, false);
    }

    public LayerStyle(String layerName, float size, RenderMode renderMode, float red, float green, float blue,
                      float alpha, float scrollU, float scrollV, boolean fullBright) {
        this.layerSize = size;
        this.layerName = layerName;
        this.renderMode = renderMode;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.scrollU = scrollU;
        this.scrollV = scrollV;
        this.fullBright = fullBright;
    }

    public LayerStyle withRenderMode(RenderMode renderMode) {
        return new LayerStyle(layerName, layerSize, renderMode, red, green, blue, alpha, scrollU, scrollV, fullBright);
    }

    public LayerStyle withColor(float red, float green, float blue) {
        return new LayerStyle(layerName, layerSize, renderMode, red, green, blue, alpha, scrollU, scrollV, fullBright);
    }

    public LayerStyle withAlpha(float alpha) {
        return new LayerStyle(layerName, layerSize, renderMode, red, green, blue, alpha, scrollU, scrollV, fullBright);
    }

    public LayerStyle withScroll(float scrollU, float scrollV) {
        return new LayerStyle(layerName, layerSize, renderMode, red, green, blue, alpha, scrollU, scrollV, fullBright);
    }

    public LayerStyle withFullBright(boolean fullBright) {
        return new LayerStyle(layerName, layerSize, renderMode, red, green, blue, alpha, scrollU, scrollV, fullBright);
    }
}
