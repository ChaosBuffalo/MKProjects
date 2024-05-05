package com.chaosbuffalo.mknpc.client.render.renderers;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
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
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

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
        for (LayerStyle layer : style.getAdditionalLayers()) {
            addLayer(new MKAdditionalBipedLayer<>(this, context, modelSupplier, style, layer, entityType));
        }
        if (style.shouldDrawArmor()) {
            addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(
                    context.bakeLayer(style.getInnerArmorLocation(entityType))),
                    new HumanoidModel<>(context.bakeLayer(style.getOuterArmorLocation(entityType))), context.getModelManager()));
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


    private HumanoidModel.ArmPose getArmPose(T entity, InteractionHand pHand) {
        ItemStack itemstack = entity.getItemInHand(pHand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (entity.getUsedItemHand() == pHand && entity.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useanim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (useanim == UseAnim.CROSSBOW && pHand == entity.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (useanim == UseAnim.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (useanim == UseAnim.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }
            } else if (!entity.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            HumanoidModel.ArmPose forgeArmPose = IClientItemExtensions.of(itemstack).getArmPose(entity, pHand, itemstack);
            if (forgeArmPose != null) return forgeArmPose;

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    private void setModelProperties(T entity) {
        HumanoidModel.ArmPose mainHandPose = getArmPose(entity, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offHandPose = getArmPose(entity, InteractionHand.OFF_HAND);
        if (mainHandPose.isTwoHanded()) {
            offHandPose = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainHandPose;
            model.leftArmPose = offHandPose;
        } else {
            model.rightArmPose = offHandPose;
            model.leftArmPose = mainHandPose;
        }

    }


    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        this.setModelProperties(entityIn);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        MKEntity.VisualCastState castState = entityIn.getVisualCastState();
        if (castState == MKEntity.VisualCastState.CASTING || castState == MKEntity.VisualCastState.RELEASE) {
            MKAbilityInfo abilityInfo = entityIn.getCastingAbility();
            if (abilityInfo != null && abilityInfo.getAbility().hasCastingParticles()) {
                ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(abilityInfo.getAbility().getCastingParticles());
                if (anim != null) {
                    Optional<Vec3> leftPos = getHandPosition(partialTicks, entityIn, HumanoidArm.LEFT);
                    leftPos.ifPresent(pos -> anim.spawn(entityIn.getCommandSenderWorld(), pos, null));
                    Optional<Vec3> rightPos = getHandPosition(partialTicks, entityIn, HumanoidArm.RIGHT);
                    rightPos.ifPresent(pos -> anim.spawn(entityIn.getCommandSenderWorld(), pos, null));
                }
            }
        }
        entityIn.getParticleEffectTracker().getParticleInstances().forEach(instance -> {
            instance.update(entityIn, skeleton, partialTicks, getRenderOffset(entityIn, partialTicks));
        });
    }

    private Optional<Vec3> getHandPosition(float partialTicks, T entityIn, HumanoidArm handSide) {
        return MCBone.getPositionOfBoneInWorld(entityIn, skeleton, partialTicks,
                getRenderOffset(entityIn, partialTicks), handSide == HumanoidArm.LEFT ?
                        BipedSkeleton.LEFT_HAND_BONE_NAME : BipedSkeleton.RIGHT_HAND_BONE_NAME);
    }
}
