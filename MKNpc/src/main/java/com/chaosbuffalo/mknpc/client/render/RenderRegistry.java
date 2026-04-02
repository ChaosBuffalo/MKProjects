package com.chaosbuffalo.mknpc.client.render;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.MKBlazeModel;
import com.chaosbuffalo.mknpc.client.render.models.MKPiglinModel;
import com.chaosbuffalo.mknpc.client.render.models.MKSkeletalModel;
import com.chaosbuffalo.mknpc.client.render.models.MKSkullModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.*;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MKNpc.MODID, value = Dist.CLIENT)
public class RenderRegistry {

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerEntityRenderer(MKNpcEntityTypes.SKELETON_TYPE.get(),
                (context) -> new SkeletalGroupRenderer(context, SkeletonStyles.SKELETON_LOOKS,
                        MKNpcEntityTypes.SKELETON_TYPE.getId()));
        evt.registerEntityRenderer(MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.get(),
                (context) -> new ZombifiedPiglinGroupRenderer(context, PiglinStyles.ZOMBIFIED_PIGLIN_LOOKS,
                        MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId()));
        evt.registerEntityRenderer(MKNpcEntityTypes.BLAZE_TYPE.get(), MKBlazeRenderer::new);
        evt.registerEntityRenderer(MKNpcEntityTypes.FLYING_SKELETON_TYPE.get(),
                (context) -> new SkeletalGroupRenderer(context, SkeletonStyles.SKELETON_LOOKS,
                        MKNpcEntityTypes.FLYING_SKELETON_TYPE.getId()));
        evt.registerEntityRenderer(MKNpcEntityTypes.FLYING_SKULL_TYPE.get(), MKFlyingSkullRenderer::new);

    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModelStyles.BASIC_STYLE.registerModelLayers(event, MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyles.CLOTHES_ONLY_STYLE.registerModelLayers(event, MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyles.CLOTHES_ONLY_STYLE.registerModelLayers(event, MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyles.BASIC_STYLE.registerModelLayers(event, MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyles.CLOTHES_ARMOR_STYLE.registerModelLayers(event, MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE.registerModelLayers(event, MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));

        event.registerLayerDefinition(MKBlazeRenderer.LAYER_LOCATION, MKBlazeModel::createBodyLayer);
        ModelStyles.BASIC_STYLE.registerModelLayers(event, MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.FLYING_SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyles.CLOTHES_ONLY_STYLE.registerModelLayers(event, MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.FLYING_SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        event.registerLayerDefinition(MKFlyingSkullRenderer.LAYER_LOCATION, MKSkullModel::createMobHeadLayer);
    }
}
