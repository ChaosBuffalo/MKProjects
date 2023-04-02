package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.entities.MKAreaEffectEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;


public class EntityMKAreaEffectRenderer extends EntityRenderer<MKAreaEffectEntity> {
    public EntityMKAreaEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull MKAreaEffectEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(@Nonnull MKAreaEffectEntity entity, @Nonnull Frustum clipping, double x, double y, double z) {
        return false;
    }
}
