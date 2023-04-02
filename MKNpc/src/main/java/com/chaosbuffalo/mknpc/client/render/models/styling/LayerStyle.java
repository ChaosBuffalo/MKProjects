package com.chaosbuffalo.mknpc.client.render.models.styling;

public class LayerStyle {

    private final float layerSize;
    private final String layerName;
    private final boolean isTranslucent;

    public float getLayerSize() {
        return layerSize;
    }

    public String getLayerName() {
        return layerName;
    }

    public boolean isTranslucent() {
        return isTranslucent;
    }

    public LayerStyle(String layerName, float size){
        this(layerName, size, false);
    }

    public LayerStyle(String layerName, float size, boolean isTranslucent){
        this.layerSize = size;
        this.layerName = layerName;
        this.isTranslucent = isTranslucent;
    }
}
