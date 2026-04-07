package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKGolemModel;
import com.chaosbuffalo.mknpc.entity.MKGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class GolemGroupRenderer extends BipedGroupRenderer<MKGolemEntity, MKGolemModel<MKGolemEntity>> {
    public GolemGroupRenderer(EntityRendererProvider.Context context, ResourceLocation entityType) {
        super(context, entityType, style -> new GolemRenderer(context, style, entityType));
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(MKGolemEntity golemEntity) {
        return GolemStyles.VANILLA_IRON_GOLEM_TEXTURE;
    }
}
