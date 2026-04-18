package com.chaosbuffalo.mkcore.client.rendering.model;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedStunAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mkcore.client.rendering.animations.spell.SpellAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.EntityAnimationModule;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.PlayerCombatExtensionModule;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiPredicate;

public class MKPlayerModel extends PlayerModel<AbstractClientPlayer> {
    private final BipedStunAnimation<Player> stunAnimation = new BipedStunAnimation<>(this);
    private final BipedSkeleton<AbstractClientPlayer, MKPlayerModel> skeleton;
    private final ModelPart root;

    public MKPlayerModel(ModelPart p_170821_, boolean p_170822_) {
        super(p_170821_, p_170822_);
        this.root = p_170821_;
        Vec3 leftHandOffset = p_170822_
                ? new Vec3(0.5 / 16.0, 10.0 / 16.0, -2.0 / 16.0)
                : new Vec3(1.0 / 16.0, 10.0 / 16.0, -2.0 / 16.0);
        Vec3 rightHandOffset = p_170822_
                ? new Vec3(-0.5 / 16.0, 10.0 / 16.0, -2.0 / 16.0)
                : new Vec3(-1.0 / 16.0, 10.0 / 16.0, -2.0 / 16.0);
        this.skeleton = new BipedSkeleton<>(this, leftHandOffset, rightHandOffset);
    }

    public BipedSkeleton<AbstractClientPlayer, MKPlayerModel> getSkeleton() {
        return skeleton;
    }

    @Override
    public void setupAnim(AbstractClientPlayer entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        MKCore.getPlayer(entityIn).ifPresent(mkEntityData -> {
            this.head.zRot = 0.0f;
            AdditionalBipedAnimation<Player> animation = getAdditionalAnimation(mkEntityData);
            if (animation != null) {
                animation.apply(entityIn);
            } else {
                applySpellAnimation(entityIn, mkEntityData, ageInTicks, netHeadYaw, headPitch);
            }
        });
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
    }


    @Override
    protected void setupAttackAnimation(AbstractClientPlayer entityIn, float ageInTicks) {
        ItemStack itemStack = entityIn.getMainHandItem();
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof BowItem) {
            super.setupAttackAnimation(entityIn, ageInTicks);
            return;
        }

        MKCore.getPlayer(entityIn).ifPresentOrElse(playerData -> {
            float partialTicks = ageInTicks - entityIn.tickCount;
            float mainSwing = playerData.getCombatExtension().getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
            float offSwing = playerData.getCombatExtension().getVisualMeleeAttackAnim(InteractionHand.OFF_HAND, partialTicks);
            boolean mainSwingActive = playerData.getCombatExtension().hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTicks);
            boolean offSwingActive = playerData.getCombatExtension().hasActiveVisualMeleeAttack(InteractionHand.OFF_HAND, partialTicks);
            boolean dualWielding = playerData.getCombatExtension().isDualWieldingMeleeWeapons();
            boolean applied = false;
            if (mainSwingActive) {
                applied |= applyMeleeAnimationPose(entityIn, InteractionHand.MAIN_HAND, mainSwing, ageInTicks,
                        playerData.getCombatExtension().getCurrentStrikePoseIndex(InteractionHand.MAIN_HAND), dualWielding);
            }
            if (offSwingActive) {
                applied |= applyMeleeAnimationPose(entityIn, InteractionHand.OFF_HAND, offSwing, ageInTicks,
                        playerData.getCombatExtension().getCurrentStrikePoseIndex(InteractionHand.OFF_HAND), dualWielding);
            }
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
                float swing = this.attackTime;
                if (swing > 0.0F) {
                    InteractionHand attackHand = entityIn.swingingArm == InteractionHand.OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                    int variant = Math.max(0, playerData.getCombatExtension().getCurrentPrimarySwingVariant(attackHand) - 1);
                    if (!applyMeleeAnimationPose(entityIn, attackHand, swing, ageInTicks, variant, dualWielding)) {
                        super.setupAttackAnimation(entityIn, ageInTicks);
                    }
                } else {
                    super.setupAttackAnimation(entityIn, ageInTicks);
                }
            }
        }, () -> super.setupAttackAnimation(entityIn, ageInTicks));
    }

    private boolean applyMeleeAnimationPose(AbstractClientPlayer entityIn, InteractionHand hand, float swing, float ageInTicks, int variant,
                                            boolean dualWielding) {
        HumanoidArm poseMainArm = getPoseMainArm(entityIn, hand);
        return MeleeAnimationManager.applyResolvedStrikePose(skeleton, entityIn, hand, MeleeAnimationManager.BIPED_FAMILY, variant,
                ModelPoseAnimator.Context.strike(swing, ageInTicks, poseMainArm, hand, dualWielding));
    }

    private void applyVanillaAttackAnimation(AbstractClientPlayer entityIn, InteractionHand hand, float swing, float ageInTicks) {
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

    private HumanoidArm getPoseMainArm(AbstractClientPlayer entityIn, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
        super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);

    }

    public AdditionalBipedAnimation<Player> getAdditionalAnimation(MKPlayerData playerData) {
        if (playerData.getEffects().isEffectActive(CoreEffects.STUN.get())) {
            return stunAnimation;
        }
        return null;
    }

    private void applySpellAnimation(AbstractClientPlayer entityIn, MKPlayerData playerData, float ageInTicks,
                                     float netHeadYaw, float headPitch) {
        EntityAnimationModule animationModule = playerData.getAnimationModule();
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

        PlayerCombatExtensionModule combatExtension = playerData.getCombatExtension();
        BiPredicate<String, String> targetFilter = createSpellTargetFilter(entityIn, combatExtension);
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

    private BiPredicate<String, String> createSpellTargetFilter(AbstractClientPlayer entityIn, PlayerCombatExtensionModule combatExtension) {
        boolean blockMainArm = combatExtension.hasVisualMeleeAttackSequence(InteractionHand.MAIN_HAND);
        boolean blockOffArm = combatExtension.hasVisualMeleeAttackSequence(InteractionHand.OFF_HAND);
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
