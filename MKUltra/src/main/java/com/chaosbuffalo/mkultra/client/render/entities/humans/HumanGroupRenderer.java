package com.chaosbuffalo.mkultra.client.render.entities.humans;

import com.chaosbuffalo.mknpc.client.render.models.MKBipedModel;
import com.chaosbuffalo.mknpc.client.render.renderers.BipedGroupRenderer;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.entities.humans.HumanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class HumanGroupRenderer extends BipedGroupRenderer<HumanEntity, MKBipedModel<HumanEntity>> {

    public HumanGroupRenderer(EntityRendererProvider.Context context, ResourceLocation entityType) {
        super(context, entityType,
                style -> new HumanRenderer(context, style, entityType));
    }

    @Nonnull
    @Override
    public ResourceLocation getBaseTexture(HumanEntity humanEntity) {
        return MKUHumans.HUMAN_SKIN_1;
    }


}
