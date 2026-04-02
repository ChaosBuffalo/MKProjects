package com.chaosbuffalo.mkultra.client.render.entities;

import com.chaosbuffalo.mkcore.client.rendering.entities.SpriteProjectileRenderer;
import com.chaosbuffalo.mknpc.client.render.models.MKBipedModel;
import com.chaosbuffalo.mknpc.client.render.models.MKGolemModel;
import com.chaosbuffalo.mknpc.client.render.models.MKPiglinModel;
import com.chaosbuffalo.mknpc.client.render.models.MKSkeletalModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyleClient;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.SkeletalGroupRenderer;
import com.chaosbuffalo.mknpc.client.render.renderers.ZombifiedPiglinGroupRenderer;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.entities.golems.GolemGroupRenderer;
import com.chaosbuffalo.mkultra.client.render.entities.humans.HumanGroupRenderer;
import com.chaosbuffalo.mkultra.client.render.entities.orcs.OrcGroupRenderer;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUModelStyles;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.List;

/**
 * Created by Jacob on 7/15/2016.
 */
@EventBusSubscriber(modid = MKUltra.MODID, value = Dist.CLIENT)
public class MKURenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerEntityRenderer(MKUEntities.ORC_TYPE.get(), (context) -> new OrcGroupRenderer(context, MKUEntities.ORC_TYPE.getId()));
        evt.registerEntityRenderer(MKUEntities.HUMAN_TYPE.get(), (context) -> new HumanGroupRenderer(context, MKUEntities.HUMAN_TYPE.getId()));
        evt.registerEntityRenderer(MKUEntities.HYBOREAN_SKELETON_TYPE.get(), (context) ->
                new SkeletalGroupRenderer(context, MKUEntities.HYBOREAN_SKELETON_TYPE.getId()));
        evt.registerEntityRenderer(MKUEntities.ZOMBIFIED_PIGLIN_TYPE.get(),
                (context) -> new ZombifiedPiglinGroupRenderer(context, MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId()));
        evt.registerEntityRenderer(MKUEntities.GOLEM_TYPE.get(), (context) -> new GolemGroupRenderer(context, MKUEntities.GOLEM_TYPE.getId()));
        evt.registerEntityRenderer(MKUEntities.HUMAN_GHOST_TYPE.get(), (context) -> new HumanGroupRenderer(context, MKUEntities.HUMAN_GHOST_TYPE.getId()));
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {

        List<ModelStyle> orcStyles = List.of(ModelStyles.LONG_HAIR_STYLE.get(), ModelStyles.ARMORED_LONG_HAIR_STYLE.get(), ModelStyles.BASIC_STYLE.get());
        for (ModelStyle style : orcStyles) {
            ModelStyleClient.registerModelLayers(event, style, MKBipedModel::createBodyLayer, MKUEntities.ORC_TYPE.getId(),
                    64, 32,
                    new ModelArgs(CubeDeformation.NONE, false, 0.0f,
                            LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        }

        List<ModelStyle> humanStyles = List.of(ModelStyles.BASIC_STYLE.get(), MKUModelStyles.TWO_LAYER_ARMOR_NO_HAIR.get(),
                MKUModelStyles.TWO_LAYER_ARMOR_SHORT_HAIR.get(), MKUModelStyles.GHOST_LONG_HAIR.get(), MKUModelStyles.TWO_LAYER_CLOTHES_SHORT_HAIR.get(),
                MKUModelStyles.GHOST_LONG_HAIR_ARMORED.get(), ModelStyles.SHORT_HAIR_STYLE.get(), MKUModelStyles.GHOST_LONG_HAIR_NO_CLOTHES.get(),
                MKUModelStyles.GHOST_LONG_HAIR_NO_CLOTHES_ARMORED.get(), MKUModelStyles.GHOST_SHORT_HAIR_NO_CLOTHES.get(),
                MKUModelStyles.GHOST_SHORT_HAIR_NO_CLOTHES_ARMORED.get());

        for (ModelStyle style : humanStyles) {
            ModelStyleClient.registerModelLayers(event, style, MKBipedModel::createBodyLayer,
                    MKUEntities.HUMAN_TYPE.getId(), 64, 32,
                    new ModelArgs(CubeDeformation.NONE, false, 0.0f,
                            LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
            ModelStyleClient.registerModelLayers(event, style, MKBipedModel::createBodyLayer,
                    MKUEntities.HUMAN_GHOST_TYPE.getId(), 64, 32,
                    new ModelArgs(CubeDeformation.NONE, false, 0.0f,
                            LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        }

        List<ModelStyle> skeletonStyles = List.of(ModelStyles.BASIC_STYLE.get(), ModelStyles.CLOTHES_ONLY_STYLE.get());
        for (ModelStyle style : skeletonStyles) {
            ModelStyleClient.registerModelLayers(event, style, MKSkeletalModel::createBodyLayer,
                    MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), 64, 32,
                    new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                            LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        }

        List<ModelStyle> zombiePiglinStyles = List.of(ModelStyles.BASIC_STYLE.get(), ModelStyles.CLOTHES_ONLY_STYLE.get(),
                ModelStyles.CLOTHES_ARMOR_STYLE.get(), ModelStyles.CLOTHES_ARMOR_TRANSLUCENT_STYLE.get());
        for (ModelStyle style : zombiePiglinStyles) {
            ModelStyleClient.registerModelLayers(event, style, MKPiglinModel::createMesh,
                    MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId(), 64, 64,
                    new ModelArgs(CubeDeformation.NONE, true, 0.0f,
                            new CubeDeformation(1.02F), LayerDefinitions.INNER_ARMOR_DEFORMATION));
        }

        List<ModelStyle> golemStyles = List.of(MKUModelStyles.BASIC_GOLEM_STYLE.get());
        for (ModelStyle style : golemStyles) {
            ModelStyleClient.registerModelLayers(event, style, MKGolemModel::createBodyLayer,
                    MKUEntities.GOLEM_TYPE.getId(), 128, 128,
                    new ModelArgs(CubeDeformation.NONE, false, 0.0f,
                            LayerDefinitions.OUTER_ARMOR_DEFORMATION, LayerDefinitions.INNER_ARMOR_DEFORMATION));
        }


    }
}

