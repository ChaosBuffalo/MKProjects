package com.chaosbuffalo.mkcore.client.rendering.entities;

import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.entities.IMKRenderAsItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SpriteProjectileRenderer<T extends AbilityProjectileEntity & IMKRenderAsItem> extends MKSpriteRenderer<T> {

    public SpriteProjectileRenderer(EntityRendererProvider.Context context, float scaleIn, boolean doBlockLightIn) {
        super(context, scaleIn, doBlockLightIn);
    }

    public SpriteProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        entityIn.clientGraphicalUpdate(partialTicks);
    }
}
