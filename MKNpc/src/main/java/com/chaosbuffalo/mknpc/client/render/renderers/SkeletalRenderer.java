package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKSkeletalModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.entity.MKSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class SkeletalRenderer extends MKBipedRenderer<MKEntity, MKSkeletalModel<MKEntity>> {


    public SkeletalRenderer(EntityRendererProvider.Context context, ModelStyle style,
                            ResourceLocation entityType) {
        super(context, style, SkeletonStyles.DEFAULT_LOOK, 0.5f, MKSkeletalModel::new, entityType);
    }

}
