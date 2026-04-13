package com.chaosbuffalo.mknpc.client.render.models.layers;

import com.chaosbuffalo.mknpc.client.render.models.styling.LayerStyle;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyleClient;
import com.chaosbuffalo.mknpc.client.render.renderers.ILayerTextureProvider;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.function.Function;

public class MKAdditionalBipedLayer<T extends MKEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final LayerStyle style;
    private final HumanoidModel<T> layerModel;
    private final ILayerTextureProvider<T, M> renderer;

    public MKAdditionalBipedLayer(ILayerTextureProvider<T, M> entityRendererIn,
                                  EntityRendererProvider.Context context,
                                  Function<ModelPart, M> modelSupplier,
                                  ModelStyle style, LayerStyle layer, ResourceLocation entityType) {
        super(entityRendererIn);
        this.renderer = entityRendererIn;
        this.layerModel = modelSupplier.apply(context.bakeLayer(ModelStyleClient.getLayerLocation(entityType, style, layer)));
        this.style = layer;
    }

    protected static <T extends MKEntity> void renderCopyLayer(HumanoidModel<T> modelParentIn, HumanoidModel<T> modelIn,
                                                               ResourceLocation textureLocationIn, PoseStack matrixStackIn,
                                                               MultiBufferSource bufferIn, int packedLightIn, T entityIn,
                                                               float limbSwing, float limbSwingAmount, float ageInTicks,
                                                               float netHeadYaw, float headPitch, float partialTicks,
                                                               LayerStyle style) {
        if (!entityIn.isInvisible() || entityIn.isGhost()) {
            modelParentIn.copyPropertiesTo(modelIn);
            modelIn.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
            modelIn.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            renderLayerModel(modelIn, textureLocationIn, matrixStackIn, bufferIn, packedLightIn, entityIn, ageInTicks, style);
        }
    }

    protected static <T extends MKEntity> void renderLayerModel(HumanoidModel<T> modelIn, ResourceLocation textureLocationIn,
                                                                PoseStack matrixStackIn, MultiBufferSource bufferIn,
                                                                int packedLightIn, T entityIn, float ageInTicks,
                                                                LayerStyle style) {
        VertexConsumer vertexBuilder = bufferIn.getBuffer(resolveRenderType(textureLocationIn, ageInTicks, style));
        int lightValue = style.isFullBright() ? LightTexture.FULL_BRIGHT : packedLightIn;
        int color = FastColor.ARGB32.colorFromFloat(
                Mth.clamp(style.getAlpha(), 0.0f, 1.0f),
                Mth.clamp(style.getRed(), 0.0f, 1.0f),
                Mth.clamp(style.getGreen(), 0.0f, 1.0f),
                Mth.clamp(style.getBlue(), 0.0f, 1.0f));
        modelIn.renderToBuffer(matrixStackIn, vertexBuilder, lightValue, LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F), color);
    }

    protected static RenderType resolveRenderType(ResourceLocation textureLocationIn, float ageInTicks, LayerStyle style) {
        return switch (style.getRenderMode()) {
            case TRANSLUCENT -> RenderType.entityTranslucent(textureLocationIn, false);
            case ENERGY_SWIRL -> RenderType.energySwirl(textureLocationIn,
                    ageInTicks * style.getScrollU(), ageInTicks * style.getScrollV());
            case CUTOUT -> RenderType.entityCutoutNoCull(textureLocationIn);
        };
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
                       T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        ResourceLocation texture = renderer.getLayerTexture(style.getLayerName(), entitylivingbaseIn);
        if (texture != null) {
            renderCopyLayer(
                    this.getParentModel(), this.layerModel,
                    texture,
                    matrixStackIn, bufferIn,
                    packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw,
                    headPitch, partialTicks, style
            );
        }
    }
}
