package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKPiglinModel;
import com.chaosbuffalo.mknpc.entity.MKZombifiedPiglinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class ZombifiedPiglinGroupRenderer extends BipedGroupRenderer<MKZombifiedPiglinEntity, MKPiglinModel<MKZombifiedPiglinEntity>> {

    public ZombifiedPiglinGroupRenderer(EntityRendererProvider.Context context, ResourceLocation entityType) {
        super(context, entityType,
                style -> new ZombifiedPiglinRenderer(context, style, entityType));
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(MKZombifiedPiglinEntity entity) {
        return PiglinStyles.VANILLA_ZOMBIFIED_PIGLIN_TEXTURE;
    }
}
