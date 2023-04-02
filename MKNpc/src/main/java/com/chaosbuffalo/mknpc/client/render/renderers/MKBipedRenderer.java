package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.models.layers.MKAdditionalBipedLayer;
import com.chaosbuffalo.mknpc.client.render.models.styling.LayerStyle;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelLook;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelStyle;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

public class MKBipedRenderer<T extends MKEntity, M extends HumanoidModel<T>> extends HumanoidMobRenderer<T, M> implements ILayerTextureProvider<T, M> {
    private final ModelStyle style;
    private final float defaultShadowSize;
    private ModelLook look;
    private final ModelLook defaultLook;
    private final BipedSkeleton<T, M> skeleton;
    

    public MKBipedRenderer(EntityRendererProvider.Context context, ModelStyle style, ModelLook defaultLook,
                           float shadowSize, Function<ModelPart, M> modelSupplier, ResourceLocation entityType) {
        super(context, modelSupplier.apply(context.bakeLayer(style.getBaseLocation(entityType))), shadowSize);
        this.style = style;
        this.defaultShadowSize = shadowSize;
        this.defaultLook = defaultLook;
        this.skeleton = new BipedSkeleton<>(getModel());
        for (LayerStyle layer : style.getAdditionalLayers()){
            addLayer(new MKAdditionalBipedLayer<>(this, context, modelSupplier, style, layer, entityType));
        }
        if (style.shouldDrawArmor()){
            addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(
                    context.bakeLayer(style.getInnerArmorLocation(entityType))),
                    new HumanoidModel<>(context.bakeLayer(style.getOuterArmorLocation(entityType)))));
        }

    }

    public void setLook(ModelLook look) {
        this.look = look;
    }

    public ModelLook getLook() {
        return look != null ? look : defaultLook;
    }

    public ModelStyle getStyle() {
        return style;
    }

    @Override
    protected void scale(T entity, PoseStack matrixStackIn, float partialTickTime) {
        float scale = entity.getScale();
        this.shadowRadius = defaultShadowSize * scale;
        matrixStackIn.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getLayerTexture(String layerName, T entity) {
        ResourceLocation tex = getLook().getLayerTexture(layerName);
        if (tex == null) {
            MKNpc.LOGGER.error("Layer texture {} missing for {}", layerName, entity);
        }
        return tex != null ? tex : ModelLook.MISSING_TEXTURE;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return getLook().getBaseTexture();
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        MKEntity.VisualCastState castState = entityIn.getVisualCastState();
        if (castState == MKEntity.VisualCastState.CASTING || castState == MKEntity.VisualCastState.RELEASE){
            MKAbility ability = entityIn.getCastingAbility();
            if (ability != null){
                if (ability.hasCastingParticles()){
                    ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(ability.getCastingParticles());
                    if (anim != null){
                        Optional<Vec3> leftPos = getHandPosition(partialTicks, entityIn, HumanoidArm.LEFT);
                        leftPos.ifPresent(pos -> anim.spawn(entityIn.getCommandSenderWorld(), pos, null));
                        Optional<Vec3> rightPos = getHandPosition(partialTicks, entityIn, HumanoidArm.RIGHT);
                        rightPos.ifPresent(pos -> anim.spawn(entityIn.getCommandSenderWorld(), pos, null));
                    }
                }
            }
        }
        entityIn.getParticleEffectTracker().getParticleInstances().forEach(instance -> {
            instance.update(entityIn, skeleton, partialTicks, getRenderOffset(entityIn, partialTicks));
        });
    }

    private Optional<Vec3> getHandPosition(float partialTicks, T entityIn, HumanoidArm handSide){
        return MCBone.getPositionOfBoneInWorld(entityIn, skeleton, partialTicks,
                getRenderOffset(entityIn, partialTicks), handSide == HumanoidArm.LEFT ?
                        BipedSkeleton.LEFT_HAND_BONE_NAME : BipedSkeleton.RIGHT_HAND_BONE_NAME);
    }
}
