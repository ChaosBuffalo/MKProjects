package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKFireElementalModel;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class FireElementalGroupRenderer extends BipedGroupRenderer<MKEntity, MKFireElementalModel<MKEntity>> {
    public FireElementalGroupRenderer(EntityRendererProvider.Context context, ResourceLocation entityType) {
        super(context, entityType, style -> new FireElementalRenderer(context, style, entityType));
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(MKEntity entity) {
        return FireElementalStyles.DEFAULT_TEXTURE;
    }
}
