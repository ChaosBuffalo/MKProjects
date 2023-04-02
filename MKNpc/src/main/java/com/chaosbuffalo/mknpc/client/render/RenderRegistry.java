package com.chaosbuffalo.mknpc.client.render;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.MKPiglinModel;
import com.chaosbuffalo.mknpc.client.render.models.MKSkeletalModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.*;
import com.chaosbuffalo.mknpc.init.MKNpcBlocks;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MKNpc.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegistry {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        ItemBlockRenderTypes.setRenderLayer(MKNpcBlocks.MK_SPAWNER_BLOCK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(MKNpcBlocks.MK_POI_BLOCK.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerEntityRenderer(MKNpcEntityTypes.SKELETON_TYPE.get(),
                (context) -> new SkeletalGroupRenderer(context, SkeletonStyles.SKELETON_LOOKS,
                        MKNpcEntityTypes.SKELETON_TYPE.getId()));
        evt.registerEntityRenderer(MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.get(),
                (context) -> new ZombifiedPiglinGroupRenderer(context, PiglinStyles.ZOMBIFIED_PIGLIN_LOOKS,
                        MKNpcEntityTypes.ZOMBIE_PIGLIN_TYPE.getId()));
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
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
    }
}
