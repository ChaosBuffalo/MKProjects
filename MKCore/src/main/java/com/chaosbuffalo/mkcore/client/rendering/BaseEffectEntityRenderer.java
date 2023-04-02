package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.entities.BaseEffectEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class BaseEffectEntityRenderer extends EntityRenderer<BaseEffectEntity> {
    public BaseEffectEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(BaseEffectEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }


    @Override
    public boolean shouldRender(@Nonnull BaseEffectEntity entity, @Nonnull Frustum clippingHelper, double x, double y, double z) {
        return true;
    }
}
