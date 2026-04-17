package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedStunAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mkcore.client.rendering.animations.spell.SpellAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.core.EntityAnimationModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.combat.MKMeleeManager;
import com.chaosbuffalo.mkcore.init.CoreEffects;
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

import java.util.function.BiPredicate;
import java.util.function.Function;

public class MKBipedModel<T extends MKEntity> extends HumanoidModel<T> {
    private final BipedStunAnimation<MKEntity> stunAnimation = new BipedStunAnimation<>(this);
    protected MCSkeleton skeleton;


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

    public MCSkeleton getSkeleton() {
        return skeleton;
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
            float mainWindupProgress = entityIn.getMeleeWindupProgress(InteractionHand.MAIN_HAND, partialTicks);
            float offWindupProgress = entityIn.getMeleeWindupProgress(InteractionHand.OFF_HAND, partialTicks);
            if (mainWindupProgress > 0.0F) {
                applyMeleeWindupPose(entityIn, InteractionHand.MAIN_HAND, mainWindupProgress);
            }
            if (offWindupProgress > 0.0F) {
                applyMeleeWindupPose(entityIn, InteractionHand.OFF_HAND, offWindupProgress);
            }
        }
        this.head.zRot = 0.0f;
        AdditionalBipedAnimation<MKEntity> animation = getAdditionalAnimation(entityIn);
        if (animation != null) {
            animation.apply(entityIn);
        } else {
            applySpellAnimation(entityIn, ageInTicks, netHeadYaw, headPitch);
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

    protected void applyMeleeWindupPose(T entityIn, InteractionHand hand, float windupProgress) {
        boolean dualWielding = MKMeleeManager.canUseForAttack(entityIn, InteractionHand.MAIN_HAND) &&
                MKMeleeManager.canUseForAttack(entityIn, InteractionHand.OFF_HAND);
        HumanoidArm poseMainArm = hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
        MeleeAnimationManager.applyResolvedWindupPose(skeleton, entityIn, hand,
                MeleeAnimationManager.BIPED_FAMILY, entityIn.getCurrentMeleeWindupVariant(hand),
                ModelPoseAnimator.Context.windup(windupProgress, poseMainArm, hand, dualWielding));
    }

    public AdditionalBipedAnimation<MKEntity> getAdditionalAnimation(T entityIn) {
        IMKEntityData entityData = MKCore.getEntityData(entityIn).orElseThrow(NullPointerException::new);
        if (entityData.getEffects().isEffectActive(CoreEffects.STUN.get())) {
            return stunAnimation;
        }
        return null;
    }

    protected void applySpellAnimation(T entityIn, float ageInTicks, float netHeadYaw, float headPitch) {
        IMKEntityData entityData = MKCore.getEntityData(entityIn).orElseThrow(NullPointerException::new);
        EntityAnimationModule animationModule = entityData.getAnimationModule();
        if (animationModule.getCastingAbility() == null) {
            return;
        }

        float progress = switch (animationModule.getVisualCastState()) {
            case CASTING -> animationModule.getCastRatio();
            case RELEASE -> animationModule.getReleaseRatio();
            case NONE -> 0.0F;
        };
        if (progress <= 0.0F || animationModule.getVisualCastState() == EntityAnimationModule.VisualCastState.NONE) {
            return;
        }

        BiPredicate<String, String> targetFilter = createSpellTargetFilter(entityIn);
        ModelPoseAnimator.Context context = ModelPoseAnimator.Context.windup(progress, ageInTicks, netHeadYaw, headPitch,
                entityIn.getMainArm(), InteractionHand.MAIN_HAND, false);
        switch (animationModule.getVisualCastState()) {
            case CASTING -> SpellAnimationManager.applyCastingPose(skeleton, entityIn, SpellAnimationManager.BIPED_FAMILY,
                    animationModule.getCastingAbility().getCastAnimationCategory(), context, targetFilter);
            case RELEASE -> SpellAnimationManager.applyReleasePose(skeleton, entityIn, SpellAnimationManager.BIPED_FAMILY,
                    animationModule.getCastingAbility().getCastAnimationCategory(), context, targetFilter);
            case NONE -> {
            }
        }
    }

    protected BiPredicate<String, String> createSpellTargetFilter(T entityIn) {
        boolean blockMainArm = entityIn.hasVisualMeleeAttackSequence(InteractionHand.MAIN_HAND);
        boolean blockOffArm = entityIn.hasVisualMeleeAttackSequence(InteractionHand.OFF_HAND);
        String mainArmTarget = entityIn.getMainArm() == HumanoidArm.RIGHT ? BipedSkeleton.RIGHT_ARM_BONE_NAME : BipedSkeleton.LEFT_ARM_BONE_NAME;
        String offArmTarget = entityIn.getMainArm() == HumanoidArm.RIGHT ? BipedSkeleton.LEFT_ARM_BONE_NAME : BipedSkeleton.RIGHT_ARM_BONE_NAME;
        return (originalTarget, resolvedTarget) -> {
            if (blockMainArm && resolvedTarget.equals(mainArmTarget)) {
                return false;
            }
            if (blockOffArm && resolvedTarget.equals(offArmTarget)) {
                return false;
            }
            return true;
        };
    }
}
