package com.chaosbuffalo.mknpc.client.render.models.layers;

import com.chaosbuffalo.mknpc.client.render.models.styling.LayerStyle;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.client.render.renderers.ILayerTextureProvider;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class MKAdditionalBipedLayer<T extends MKEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final LayerStyle style;
    private final EntityModel<T> layerModel;
    private final ILayerTextureProvider<T, M> renderer;

    public MKAdditionalBipedLayer(ILayerTextureProvider<T, M> entityRendererIn,
                                  EntityRendererProvider.Context context,
                                  Function<ModelPart, M> modelSupplier,
                                  ModelStyle style, LayerStyle layer, ResourceLocation entityType) {
        super(entityRendererIn);
        this.renderer = entityRendererIn;
        this.layerModel = modelSupplier.apply(context.bakeLayer(style.getLayerLocation(entityType, layer)));
        this.style = layer;
    }

    protected static <T extends MKEntity> void renderCopyTranslucent(EntityModel<T> modelParentIn, EntityModel<T> modelIn,
                                                                     ResourceLocation textureLocationIn, PoseStack matrixStackIn,
                                                                     MultiBufferSource bufferIn, int packedLightIn, T entityIn,
                                                                     float limbSwing, float limbSwingAmount, float ageInTicks,
                                                                     float netHeadYaw, float headPitch, float partialTicks,
                                                                     float red, float green, float blue) {
        if (!entityIn.isInvisible() || entityIn.isGhost()) {
            modelParentIn.copyPropertiesTo(modelIn);
            modelIn.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
            modelIn.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            renderTranslucentModel(modelIn, textureLocationIn, matrixStackIn, bufferIn, packedLightIn, entityIn, red, green, blue);
        }

    }

    protected static <T extends MKEntity> void renderTranslucentModel(EntityModel<T> modelIn, ResourceLocation textureLocationIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entityIn, float red, float green, float blue) {
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityTranslucent(textureLocationIn, false));
        modelIn.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F), red, green, blue, 1.0F);
    }

    protected static <T extends MKEntity> void renderCopyCutoutModel(EntityModel<T> modelParentIn, EntityModel<T> modelIn,
                                                                     ResourceLocation textureLocationIn, PoseStack matrixStackIn,
                                                                     MultiBufferSource bufferIn, int packedLightIn, T entityIn,
                                                                     float limbSwing, float limbSwingAmount, float ageInTicks,
                                                                     float netHeadYaw, float headPitch, float partialTicks,
                                                                     float red, float green, float blue) {
        if (!entityIn.isInvisible() || entityIn.isGhost()) {
            modelParentIn.copyPropertiesTo(modelIn);
            modelIn.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
            modelIn.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            renderColoredCutoutModel(modelIn, textureLocationIn, matrixStackIn, bufferIn, packedLightIn, entityIn, red, green, blue);
        }

    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
                       T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (style.isTranslucent()){
            renderCopyTranslucent(
                    this.getParentModel(), this.layerModel,
                    renderer.getLayerTexture(style.getLayerName(), entitylivingbaseIn),
                    matrixStackIn, bufferIn,
                    packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch, partialTicks, 1.0F, 1.0F, 1.0F
            );
        } else {
            renderCopyCutoutModel(
                    this.getParentModel(), this.layerModel,
                    renderer.getLayerTexture(style.getLayerName(), entitylivingbaseIn),
                    matrixStackIn, bufferIn,
                    packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch, partialTicks, 1.0F, 1.0F, 1.0F
            );
        }

    }
}
