package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKGolemModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.entity.MKGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class GolemRenderer extends MKBipedRenderer<MKGolemEntity, MKGolemModel<MKGolemEntity>> {
    public GolemRenderer(EntityRendererProvider.Context context, ModelStyle style, ResourceLocation entityType) {
        super(context, style, 1.25f, MKGolemModel::new, entityType);
    }

    @Override
    protected float getVisualLungeAmount(MKGolemEntity entity) {
        return 0.75f;
    }
}
