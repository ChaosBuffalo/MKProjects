package com.chaosbuffalo.mknpc.client.render;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.MKBlazeModel;
import com.chaosbuffalo.mknpc.client.render.models.MKGolemModel;
import com.chaosbuffalo.mknpc.client.render.models.MKPiglinModel;
import com.chaosbuffalo.mknpc.client.render.models.MKSkeletalModel;
import com.chaosbuffalo.mknpc.client.render.models.MKSkullModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyleClient;
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
                (context) -> new SkeletalGroupRenderer(context, MKNpcEntityTypes.SKELETON_TYPE.getId()));
        evt.registerEntityRenderer(MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.get(),
                (context) -> new ZombifiedPiglinGroupRenderer(context, MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId()));
        evt.registerEntityRenderer(MKNpcEntityTypes.BLAZE_TYPE.get(), MKBlazeRenderer::new);
        evt.registerEntityRenderer(MKNpcEntityTypes.FLYING_SKELETON_TYPE.get(),
                (context) -> new SkeletalGroupRenderer(context, MKNpcEntityTypes.FLYING_SKELETON_TYPE.getId()));
        evt.registerEntityRenderer(MKNpcEntityTypes.FLYING_SKULL_TYPE.get(), MKFlyingSkullRenderer::new);
        evt.registerEntityRenderer(MKNpcEntityTypes.GOLEM_TYPE.get(),
                (context) -> new GolemGroupRenderer(context, MKNpcEntityTypes.GOLEM_TYPE.getId()));

    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModelStyleClient.registerModelLayers(event, ModelStyles.BASIC_STYLE.get(), MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyleClient.registerModelLayers(event, ModelStyles.CLOTHES_ONLY_STYLE.get(), MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyleClient.registerModelLayers(event, ModelStyles.CLOTHES_ONLY_STYLE.get(), MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyleClient.registerModelLayers(event, ModelStyles.BASIC_STYLE.get(), MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyleClient.registerModelLayers(event, ModelStyles.CLOTHES_ARMOR_STYLE.get(), MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyleClient.registerModelLayers(event, ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE.get(), MKPiglinModel::createMesh,
                MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId(), 64, 64,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));

        event.registerLayerDefinition(MKBlazeRenderer.LAYER_LOCATION, MKBlazeModel::createBodyLayer);
        ModelStyleClient.registerModelLayers(event, ModelStyles.BASIC_STYLE.get(), MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.FLYING_SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyleClient.registerModelLayers(event, ModelStyles.CLOTHES_ONLY_STYLE.get(), MKSkeletalModel::createBodyLayer,
                MKNpcEntityTypes.FLYING_SKELETON_TYPE.getId(), 64, 32,
                new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        ModelStyleClient.registerModelLayers(event, ModelStyles.BASIC_GOLEM_STYLE.get(), MKGolemModel::createBodyLayer,
                MKNpcEntityTypes.GOLEM_TYPE.getId(), 128, 128,
                new ModelArgs(CubeDeformation.NONE, false, 0.0f,
                        LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        event.registerLayerDefinition(MKFlyingSkullRenderer.LAYER_LOCATION, MKSkullModel::createMobHeadLayer);
    }
}

