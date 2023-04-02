package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mknpc.client.render.models.MKPiglinModel;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.entity.MKZombifiedPiglinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ZombifiedPiglinRenderer extends MKBipedRenderer<MKZombifiedPiglinEntity, MKPiglinModel<MKZombifiedPiglinEntity>> {


    public ZombifiedPiglinRenderer(EntityRendererProvider.Context context, ModelStyle style,
                                   ResourceLocation entityType) {
        super(context, style, PiglinStyles.DEFAULT_ZOMBIE_LOOK, 0.5f, MKPiglinModel::new, entityType);
    }
}
