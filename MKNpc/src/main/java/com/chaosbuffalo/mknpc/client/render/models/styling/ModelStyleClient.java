package com.chaosbuffalo.mknpc.client.render.models.styling;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ModelStyleClient {
    private static final Map<ResourceLocation, Map<String, ModelStyle>> REGISTERED_ENTITY_STYLES = new HashMap<>();

    public static ModelLayerLocation getLayerLocation(ResourceLocation entityTypeName, ModelStyle style, LayerStyle layerStyle) {
        return new ModelLayerLocation(entityTypeName, String.format("%s.%s", style.getName(), layerStyle.getLayerName()));
    }

    public static ModelLayerLocation getBaseLocation(ResourceLocation entityTypeName, ModelStyle style) {
        return new ModelLayerLocation(entityTypeName, style.getName());
    }

    public static ModelLayerLocation getInnerArmorLocation(ResourceLocation entityTypeName, ModelStyle style) {
        return new ModelLayerLocation(entityTypeName, String.format("%s.inner_armor", style.getName()));
    }

    public static ModelLayerLocation getOuterArmorLocation(ResourceLocation entityTypeName, ModelStyle style) {
        return new ModelLayerLocation(entityTypeName, String.format("%s.outer_armor", style.getName()));
    }

    public static synchronized List<ModelStyle> getRegisteredStyles(ResourceLocation entityTypeName) {
        Map<String, ModelStyle> registered = REGISTERED_ENTITY_STYLES.get(entityTypeName);
        if (registered == null || registered.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(registered.values());
    }

    private static synchronized void registerStyle(ResourceLocation entityTypeName, ModelStyle style) {
        REGISTERED_ENTITY_STYLES.computeIfAbsent(entityTypeName, key -> new LinkedHashMap<>())
                .put(style.getName(), style);
    }

    public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event,
                                           ModelStyle style,
                                           Function<ModelArgs, MeshDefinition> layerProvider,
                                           ResourceLocation entityTypeName, int textureWidth, int textureHeight, ModelArgs args) {
        registerStyle(entityTypeName, style);
        event.registerLayerDefinition(getBaseLocation(entityTypeName, style),
                () -> LayerDefinition.create(layerProvider.apply(args), textureWidth, textureHeight));
        if (style.shouldDrawArmor()) {
            event.registerLayerDefinition(getOuterArmorLocation(entityTypeName, style),
                    () -> LayerDefinition.create(HumanoidModel.createMesh(args.outerArmorDeformation, 0.0f), 64, 32));
            event.registerLayerDefinition(getInnerArmorLocation(entityTypeName, style),
                    () -> LayerDefinition.create(HumanoidModel.createMesh(args.innerArmorDeformation, 0.0f), 64, 32));
        }
        for (LayerStyle layerStyle : style.getAdditionalLayers()) {
            event.registerLayerDefinition(getLayerLocation(entityTypeName, style, layerStyle),
                    () -> LayerDefinition.create(layerProvider.apply(
                                    args.copyWithOverrides(false, new CubeDeformation(layerStyle.getLayerSize()))),
                            textureWidth, textureHeight));
        }
    }
}
