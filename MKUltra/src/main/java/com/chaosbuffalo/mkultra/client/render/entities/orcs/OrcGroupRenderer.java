package com.chaosbuffalo.mkultra.client.render.entities.orcs;


import com.chaosbuffalo.mknpc.client.render.models.MKBipedModel;
import com.chaosbuffalo.mknpc.client.render.renderers.BipedGroupRenderer;
import com.chaosbuffalo.mkultra.client.render.styling.MKUOrcs;
import com.chaosbuffalo.mkultra.entities.orcs.OrcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class OrcGroupRenderer extends BipedGroupRenderer<OrcEntity, MKBipedModel<OrcEntity>> {


    public OrcGroupRenderer(EntityRendererProvider.Context context, ResourceLocation entityType) {
        super(context, entityType,
                style -> new OrcRenderer(context, style, entityType));
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(OrcEntity entity) {
        return MKUOrcs.GREEN_ORC;
    }
}
