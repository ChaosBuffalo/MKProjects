package com.chaosbuffalo.mkcore.client.rendering.entities;

import com.chaosbuffalo.mkcore.entities.IMKRenderAsItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;


public class MKSpriteRenderer<T extends Entity & IMKRenderAsItem> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean doBlockLight;

    public MKSpriteRenderer(EntityRendererProvider.Context context,
                            float scaleIn, boolean doBlockLightIn) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.scale = scaleIn;
        this.doBlockLight = doBlockLightIn;
    }


    public MKSpriteRenderer(EntityRendererProvider.Context context) {
        this(context, 1.0F, false);
    }

    protected int getBlockLightLevel(T entityIn, BlockPos pos) {
        return this.doBlockLight ? 15 : super.getBlockLightLevel(entityIn, pos);
    }

    public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {

        matrixStackIn.pushPose();
        matrixStackIn.scale(this.scale * entityIn.getScale(), this.scale * entityIn.getScale(), this.scale * entityIn.getScale());
        matrixStackIn.mulPose(this.entityRenderDispatcher.cameraOrientation());
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.itemRenderer.renderStatic(entityIn.getItem(), ItemDisplayContext.GROUND, packedLightIn, OverlayTexture.NO_OVERLAY,
                matrixStackIn, bufferIn, entityIn.getLevel(), entityIn.getId());
        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}