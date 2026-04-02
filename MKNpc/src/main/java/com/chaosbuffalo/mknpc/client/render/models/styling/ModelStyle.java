package com.chaosbuffalo.mknpc.client.render.models.styling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelStyle {
    private final List<LayerStyle> additionalLayers;

    private final boolean hasArmor;
    private final boolean hasArmorVariant;
    private final String name;

    public List<LayerStyle> getAdditionalLayers() {
        return additionalLayers;
    }

    public boolean shouldDrawArmor() {
        return hasArmor;
    }

    public boolean needsArmorVariant() {
        return hasArmorVariant;
    }

    public ModelStyle(String name, boolean hasArmor, boolean hasArmorVariant, LayerStyle... layers) {
        this.name = name;
        this.hasArmor = hasArmor;
        this.hasArmorVariant = hasArmorVariant;
        this.additionalLayers = new ArrayList<>();
        this.additionalLayers.addAll(Arrays.asList(layers));
    }

    public String getName() {
        return name;
    }


}
