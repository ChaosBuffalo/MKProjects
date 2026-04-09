package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedCastAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedStunAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.combat.MKMeleeManager;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mknpc.client.render.animations.MKEntityCompleteCastAnimation;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class MKBipedModel<T extends MKEntity> extends HumanoidModel<T> {
    private final BipedCastAnimation<MKEntity> castAnimation = new BipedCastAnimation<>(this);
    private final MKEntityCompleteCastAnimation completeCastAnimation = new MKEntityCompleteCastAnimation(this);
    private final BipedStunAnimation<MKEntity> stunAnimation = new BipedStunAnimation<>(this);
    protected final BipedSkeleton<T, MKBipedModel<T>> skeleton;


    public MKBipedModel(ModelPart modelPart) {
        super(modelPart);
        this.skeleton = new BipedSkeleton<>(this);
    }

    public MKBipedModel(ModelPart modelPart, Function<ResourceLocation, RenderType> renderSupplier) {
        super(modelPart, renderSupplier);
        this.skeleton = new BipedSkeleton<>(this);
    }


    public static MeshDefinition createBodyLayer(ModelArgs args) {
        return HumanoidModel.createMesh(args.deformation, 0.0f);
    }

    @Override
    public void prepareMobModel(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        // bow pose stuff from skeleton
        ItemStack itemstack = entityIn.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemstack.getItem() instanceof BowItem && entityIn.isAggressive()) {
            if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = ArmPose.BOW_AND_ARROW;
                this.leftArmPose = ArmPose.EMPTY;
            } else {
                this.leftArmPose = ArmPose.BOW_AND_ARROW;
                this.rightArmPose = ArmPose.EMPTY;
            }
        }
        super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // bow pose stuff from skeleton
        float partialTicks = ageInTicks - entityIn.tickCount;
        if (this.attackTime <= 0.0F &&
                !entityIn.hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTicks) &&
                !entityIn.hasActiveVisualMeleeAttack(InteractionHand.OFF_HAND, partialTicks)) {
            float windupProgress = entityIn.getMeleeWindupProgress(partialTicks);
            if (windupProgress > 0.0F) {
                applyMeleeWindupPose(entityIn, windupProgress);
            }
        }
        this.head.zRot = 0.0f;
        AdditionalBipedAnimation<MKEntity> animation = getAdditionalAnimation(entityIn);
        if (animation != null) {
            animation.apply(entityIn);
        }

    }

    @Override
    protected void setupAttackAnimation(T entityIn, float ageInTicks) {
        ItemStack itemstack = entityIn.getMainHandItem();
        float partialTicks = ageInTicks - entityIn.tickCount;
        float mainSwing = entityIn.getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
        float offSwing = entityIn.getVisualMeleeAttackAnim(InteractionHand.OFF_HAND, partialTicks);
        boolean mainSwingActive = entityIn.hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTicks);
        boolean offSwingActive = entityIn.hasActiveVisualMeleeAttack(InteractionHand.OFF_HAND, partialTicks);
        boolean dualWielding = MKMeleeManager.canUseForAttack(entityIn, InteractionHand.MAIN_HAND) &&
                MKMeleeManager.canUseForAttack(entityIn, InteractionHand.OFF_HAND);
        boolean applied = false;
        if (mainSwingActive && (itemstack.isEmpty() || !(itemstack.getItem() instanceof BowItem))) {
            applied |= applyHeavyMeleeSwing(entityIn, InteractionHand.MAIN_HAND, mainSwing, ageInTicks);
        }
        if (offSwingActive) {
            applied |= applyHeavyMeleeSwing(entityIn, InteractionHand.OFF_HAND, offSwing, ageInTicks);
        }
        if (mainSwingActive || offSwingActive) {
            if (!applied) {
                if (!dualWielding) {
                    if (mainSwingActive) {
                        applyVanillaAttackAnimation(entityIn, InteractionHand.MAIN_HAND, mainSwing, ageInTicks);
                        return;
                    }
                    if (offSwingActive) {
                        applyVanillaAttackAnimation(entityIn, InteractionHand.OFF_HAND, offSwing, ageInTicks);
                        return;
                    }
                }
                super.setupAttackAnimation(entityIn, ageInTicks);
            }
        } else {
            super.setupAttackAnimation(entityIn, ageInTicks);
        }
    }

    protected boolean applyHeavyMeleeSwing(T entityIn, InteractionHand hand, float swing, float ageInTicks) {
        boolean dualWielding = MKMeleeManager.canUseForAttack(entityIn, InteractionHand.MAIN_HAND) &&
                MKMeleeManager.canUseForAttack(entityIn, InteractionHand.OFF_HAND);
        HumanoidArm poseMainArm = hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
        return MeleeAnimationManager.applyResolvedStrikePose(skeleton, entityIn, hand, MeleeAnimationManager.BIPED_FAMILY,
                entityIn.getCurrentStrikePoseIndex(hand),
                ModelPoseAnimator.Context.strike(swing, ageInTicks, poseMainArm, hand, dualWielding));
    }

    private void applyVanillaAttackAnimation(T entityIn, InteractionHand hand, float swing, float ageInTicks) {
        float previousAttackTime = this.attackTime;
        InteractionHand previousSwingingArm = entityIn.swingingArm;
        this.attackTime = swing;
        entityIn.swingingArm = hand;
        try {
            super.setupAttackAnimation(entityIn, ageInTicks);
        } finally {
            this.attackTime = previousAttackTime;
            entityIn.swingingArm = previousSwingingArm;
        }
    }

    protected void applyMeleeWindupPose(T entityIn, float windupProgress) {
        MeleeAnimationManager.applyResolvedWindupPose(skeleton, entityIn, InteractionHand.MAIN_HAND,
                MeleeAnimationManager.BIPED_FAMILY, entityIn.getCurrentMeleeWindupVariant(),
                ModelPoseAnimator.Context.windup(windupProgress, entityIn.getMainArm(), InteractionHand.MAIN_HAND));
    }

    public AdditionalBipedAnimation<MKEntity> getAdditionalAnimation(T entityIn) {
        IMKEntityData entityData = MKCore.getEntityData(entityIn).orElseThrow(NullPointerException::new);
        if (entityData.getEffects().isEffectActive(CoreEffects.STUN.get())) {
            return stunAnimation;
        }
        switch (entityIn.getVisualCastState()) {
            case CASTING:
                return castAnimation;
            case RELEASE:
                return completeCastAnimation;
            case NONE:
            default:
                return null;
        }
    }
}
