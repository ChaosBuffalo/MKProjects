package com.chaosbuffalo.mknpc.client.render.models.styling;


import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

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

    public boolean needsArmorVariant(){
        return hasArmorVariant;
    }

    public ModelStyle(String name, boolean hasArmor, boolean hasArmorVariant, LayerStyle... layers){
        this.name = name;
        this.hasArmor = hasArmor;
        this.hasArmorVariant = hasArmorVariant;
        this.additionalLayers = new ArrayList<>();
        this.additionalLayers.addAll(Arrays.asList(layers));
    }

    public String getName() {
        return name;
    }


    public ModelLayerLocation getLayerLocation(ResourceLocation entityTypeName, LayerStyle style){
        return new ModelLayerLocation(entityTypeName, String.format("%s.%s", name, style.getLayerName()));
    }

    public ModelLayerLocation getBaseLocation(ResourceLocation entityTypeName) {
        return new ModelLayerLocation(entityTypeName, name);
    }

    public ModelLayerLocation getInnerArmorLocation(ResourceLocation entityTypeName) {
        return new ModelLayerLocation(entityTypeName, String.format("%s.inner_armor", name));
    }

    public ModelLayerLocation getOuterArmorLocation(ResourceLocation entityTypeName) {
        return new ModelLayerLocation(entityTypeName, String.format("%s.outer_armor", name));
    }

    public void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event,
                                    Function<ModelArgs, MeshDefinition> layerProvider,
                                    ResourceLocation entityTypeName, int textureWidth, int textureHeight, ModelArgs args) {
        event.registerLayerDefinition(new ModelLayerLocation(entityTypeName, name),
                () -> LayerDefinition.create(layerProvider.apply(args), textureWidth, textureHeight));
        if (shouldDrawArmor()) {
            event.registerLayerDefinition(new ModelLayerLocation(entityTypeName, String.format("%s.outer_armor", name)),
                    () -> LayerDefinition.create(HumanoidModel.createMesh(args.outerArmorDeformation, 0.0f), 64, 32));
            event.registerLayerDefinition(new ModelLayerLocation(entityTypeName, String.format("%s.inner_armor", name)),
                    () -> LayerDefinition.create(HumanoidModel.createMesh(args.innerArmorDeformation, 0.0f), 64, 32));
        }
        for (LayerStyle style : getAdditionalLayers()) {
            event.registerLayerDefinition(new ModelLayerLocation(entityTypeName, String.format("%s.%s", name, style.getLayerName())),
                    () -> LayerDefinition.create(layerProvider.apply(
                            args.copyWithOverrides(false, new CubeDeformation(style.getLayerSize()))),
                            textureWidth, textureHeight));
        }

    }
}
