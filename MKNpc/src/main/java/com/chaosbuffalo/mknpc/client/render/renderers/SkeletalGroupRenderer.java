package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKSkeletalModel;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SkeletalGroupRenderer extends BipedGroupRenderer<MKEntity, MKSkeletalModel<MKEntity>> {

    public SkeletalGroupRenderer(EntityRendererProvider.Context context, ResourceLocation entityType) {
        super(context, entityType,
                style -> new SkeletalRenderer(context, style, entityType));
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(MKEntity entity) {
        return SkeletonStyles.SKELETON_TEXTURES;
    }
}
