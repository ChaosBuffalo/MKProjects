package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKSkeletalModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyles;
import com.chaosbuffalo.mknpc.entity.MKSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;


public class SkeletalGroupRenderer extends BipedGroupRenderer<MKSkeletonEntity, MKSkeletalModel<MKSkeletonEntity>> {

    public SkeletalGroupRenderer(EntityRendererProvider.Context context, Map<String, ModelLook> styles, ResourceLocation entityType) {
        super(context);
        putRenderer(ModelStyles.BASIC_NAME, new SkeletalRenderer(context, ModelStyles.BASIC_STYLE, entityType));
        putRenderer(ModelStyles.CLOTHES_ONLY_NAME, new SkeletalRenderer(context, ModelStyles.CLOTHES_ONLY_STYLE, entityType));
        for (Map.Entry<String, ModelLook> entry : styles.entrySet()){
            putLook(entry.getKey(), entry.getValue());
        }
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(MKSkeletonEntity entity) {
        return SkeletonStyles.SKELETON_TEXTURES;
    }
}
