package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKBlazeModel;
import com.chaosbuffalo.mknpc.entity.MKBlazeEntity;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

public class MKBlazeRenderer extends MobRenderer<MKBlazeEntity, MKBlazeModel<MKBlazeEntity>> {
    private static final ResourceLocation BLAZE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/blaze.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(MKNpcEntityTypes.BLAZE_TYPE.getId(), "base");

    public MKBlazeRenderer(EntityRendererProvider.Context p_173933_) {
        super(p_173933_, new MKBlazeModel<>(p_173933_.bakeLayer(LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(MKBlazeEntity mkBlazeEntity) {
        return BLAZE_LOCATION;
    }

    protected int getBlockLightLevel(Blaze entity, BlockPos pos) {
        return 15;
    }

}
