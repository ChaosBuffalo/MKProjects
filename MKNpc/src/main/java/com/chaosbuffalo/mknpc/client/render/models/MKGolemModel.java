package com.chaosbuffalo.mknpc.client.render.models;


import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mkcore.core.combat.MKMeleeManager;
import com.chaosbuffalo.mknpc.client.render.animations.MKNpcMeleeAnimations;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.client.render.skeleton.GolemSkeleton;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.item.BowItem;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class MKGolemModel<T extends MKEntity> extends MKBipedModel<T> {
    private final ModelPart root;

    public MKGolemModel(ModelPart modelPart) {
        super(modelPart);
        this.root = modelPart;
        this.skeleton = new GolemSkeleton<>(this);
    }

    public static MeshDefinition createBodyLayer(ModelArgs args) {
        CubeDeformation deformation = args.deformation;
        float headOffset = args.heightOffset;
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F, deformation).texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F, deformation), PartPose.offset(0.0F, -7.0F, -2.0F));
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F, deformation.extend(0.5F)).texOffs(24, 20).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F, deformation), PartPose.offset(0.0F, -7.0f + headOffset, -2.0f));
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F, deformation).texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, deformation.extend(0.5f)), PartPose.offset(0.0F, -7.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, deformation), PartPose.offset(0.0F, -7.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F, deformation), PartPose.offset(0.0F, -7.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, deformation), PartPose.offset(-4.0F, 11.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F, deformation), PartPose.offset(5.0F, 11.0F, 0.0F));
        return meshdefinition;
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F);
        leftLeg.xRot = -1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;
        rightLeg.xRot = 1.5F * Mth.triangleWave(limbSwing, 13.0F) * limbSwingAmount;
        leftLeg.yRot = 0.0F;
        rightLeg.yRot = 0.0F;
        float partialTicks = ageInTicks - entityIn.tickCount;
        setupAttackAnimation(entityIn, ageInTicks);
        applyDynamicArmRootBias(entityIn, partialTicks);
        this.head.zRot = 0.0F;

        AdditionalBipedAnimation<MKEntity> animation = getAdditionalAnimation(entityIn);
        if (animation != null) {
            animation.apply(entityIn);
        }
    }

    @Override
    protected void setupAttackAnimation(T entityIn, float ageInTicks) {
        float partialTicks = ageInTicks - entityIn.tickCount;
        float mainSwing = entityIn.getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
        float offSwing = entityIn.getVisualMeleeAttackAnim(InteractionHand.OFF_HAND, partialTicks);
        boolean mainSwingActive = entityIn.hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTicks);
        boolean offSwingActive = entityIn.hasActiveVisualMeleeAttack(InteractionHand.OFF_HAND, partialTicks);

        if (mainSwingActive) {
            if (!applyHeavyMeleeSwing(entityIn, InteractionHand.MAIN_HAND, mainSwing, ageInTicks)) {
                applyDefaultStrikePose(entityIn, InteractionHand.MAIN_HAND, mainSwing, ageInTicks);
            }
        }
        if (offSwingActive && !applyHeavyMeleeSwing(entityIn, InteractionHand.OFF_HAND, offSwing, ageInTicks)) {
            if (!(entityIn.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof BowItem)) {
                applyDefaultStrikePose(entityIn, InteractionHand.OFF_HAND, offSwing, ageInTicks);
            }
        }
    }

    @Override
    public void prepareMobModel(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean mainAttackActive = entityIn.hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTick);
        boolean offAttackActive = entityIn.hasActiveVisualMeleeAttack(InteractionHand.OFF_HAND, partialTick);
        if (mainAttackActive || offAttackActive) {
            resetUpperBodyPose();
        } else if (entityIn.isBlocking()) {
            resetUpperBodyPose();
            applyBlockPose(entityIn);
        } else {
            resetUpperBodyPose();
            float mainWindupProgress = entityIn.getMeleeWindupProgress(InteractionHand.MAIN_HAND, partialTick);
            float offWindupProgress = entityIn.getMeleeWindupProgress(InteractionHand.OFF_HAND, partialTick);
            if (mainWindupProgress > 0.0F || offWindupProgress > 0.0F) {
                if (mainWindupProgress > 0.0F && !applyWeaponWindupPose(entityIn, InteractionHand.MAIN_HAND, mainWindupProgress)) {
                    applyDefaultWindupPose(entityIn, InteractionHand.MAIN_HAND, mainWindupProgress);
                }
                if (offWindupProgress > 0.0F && !applyWeaponWindupPose(entityIn, InteractionHand.OFF_HAND, offWindupProgress)) {
                    applyDefaultWindupPose(entityIn, InteractionHand.OFF_HAND, offWindupProgress);
                }
            } else {
                rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
                leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
            }
        }
    }

    private void applyBlockPose(T entityIn) {
        HumanoidArm blockArm = entityIn.getUsedItemHand() == InteractionHand.OFF_HAND
                ? entityIn.getMainArm().getOpposite()
                : entityIn.getMainArm();
        ModelPart mainBlockArm = getArm(blockArm);
        ModelPart supportArm = getArm(blockArm.getOpposite());

        body.xRot = 0.16F;
        body.yRot = blockArm == HumanoidArm.RIGHT ? -0.12F : 0.12F;

        mainBlockArm.xRot = -2.15F;
        mainBlockArm.yRot = blockArm == HumanoidArm.RIGHT ? -0.42F : 0.42F;
        mainBlockArm.zRot = blockArm == HumanoidArm.RIGHT ? -0.14F : 0.14F;

        supportArm.xRot = -1.98F;
        supportArm.yRot = blockArm == HumanoidArm.RIGHT ? 0.24F : -0.24F;
        supportArm.zRot = blockArm == HumanoidArm.RIGHT ? 0.10F : -0.10F;
    }

    private void applyDynamicArmRootBias(T entityIn, float partialTicks) {
        float rightInward = 0.35F;
        float leftInward = 0.35F;
        float forwardBias = 0.0F;

        if (entityIn.isBlocking()) {
            rightInward += 1.15F;
            leftInward += 1.15F;
            forwardBias = -0.25F;
        } else {
            float mainWindup = entityIn.getMeleeWindupProgress(InteractionHand.MAIN_HAND, partialTicks);
            float offWindup = entityIn.getMeleeWindupProgress(InteractionHand.OFF_HAND, partialTicks);
            if (mainWindup > 0.0F) {
                float pull = 0.35F + mainWindup * 0.55F;
                if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
                    rightInward += pull;
                    leftInward += pull * 0.45F;
                } else {
                    leftInward += pull;
                    rightInward += pull * 0.45F;
                }
                forwardBias = Math.min(forwardBias, -0.10F * mainWindup);
            }
            if (offWindup > 0.0F) {
                float pull = 0.35F + offWindup * 0.55F;
                if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
                    leftInward += pull;
                    rightInward += pull * 0.45F;
                } else {
                    rightInward += pull;
                    leftInward += pull * 0.45F;
                }
                forwardBias = Math.min(forwardBias, -0.10F * offWindup);
            }

            if (entityIn.hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTicks)) {
                float swing = entityIn.getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
                float pull = getStrikeArmRootPull(swing);
                if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
                    rightInward += pull;
                    leftInward += pull * 0.45F;
                } else {
                    leftInward += pull;
                    rightInward += pull * 0.45F;
                }
                forwardBias = Math.min(forwardBias, -0.10F * getStrikeForwardBias(swing));
            }

            if (entityIn.hasActiveVisualMeleeAttack(InteractionHand.OFF_HAND, partialTicks)) {
                float swing = entityIn.getVisualMeleeAttackAnim(InteractionHand.OFF_HAND, partialTicks);
                float pull = getStrikeArmRootPull(swing);
                if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
                    leftInward += pull;
                    rightInward += pull * 0.45F;
                } else {
                    rightInward += pull;
                    leftInward += pull * 0.45F;
                }
                forwardBias = Math.min(forwardBias, -0.10F * getStrikeForwardBias(swing));
            }
        }

        rightArm.x += rightInward;
        leftArm.x -= leftInward;
        rightArm.z += forwardBias;
        leftArm.z += forwardBias;
    }

    private float getStrikeArmRootPull(float swing) {
        float swingArc = Mth.sin(swing * Mth.PI);
        float impact = Mth.sin((1.0F - (1.0F - swing) * (1.0F - swing)) * Mth.PI);
        float followThrough = Mth.sin(Mth.clamp((swing - 0.45F) / 0.55F, 0.0F, 1.0F) * (Mth.PI / 2.0F));
        return 0.28F + swingArc * 0.48F + impact * 0.18F + followThrough * 0.52F;
    }

    private float getStrikeForwardBias(float swing) {
        float swingArc = Mth.sin(swing * Mth.PI);
        float followThrough = Mth.sin(Mth.clamp((swing - 0.45F) / 0.55F, 0.0F, 1.0F) * (Mth.PI / 2.0F));
        return swingArc * 0.65F + followThrough * 0.35F;
    }

    @Override
    public void translateToHand(HumanoidArm sideIn, PoseStack matrixStackIn) {
        ModelPart arm = this.getArm(sideIn);
        arm.translateAndRotate(matrixStackIn);
        float horizontalOffset = sideIn == HumanoidArm.RIGHT ? -11.0F / 16.0F : 11.0F / 16.0F;
        matrixStackIn.translate(horizontalOffset, 19.0F / 16.0F, 0.0F);
    }

    private boolean applyWeaponWindupPose(T entityIn, InteractionHand hand, float windupProgress) {
        if (!MKMeleeManager.canUseCustomMelee(entityIn, hand)) {
            return false;
        }
        boolean dualWielding = MKMeleeManager.canUseForAttack(entityIn, InteractionHand.MAIN_HAND) &&
                MKMeleeManager.canUseForAttack(entityIn, InteractionHand.OFF_HAND);
        HumanoidArm poseMainArm = hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
        return MeleeAnimationManager.applyResolvedWindupPose(skeleton, entityIn, hand,
                MeleeAnimationManager.BIPED_FAMILY, entityIn.getCurrentMeleeWindupVariant(hand),
                ModelPoseAnimator.Context.windup(windupProgress, poseMainArm, hand, dualWielding));
    }

    private void applyDefaultWindupPose(T entityIn, InteractionHand hand, float windupProgress) {
        boolean dualWielding = MKMeleeManager.canUseForAttack(entityIn, InteractionHand.MAIN_HAND) &&
                MKMeleeManager.canUseForAttack(entityIn, InteractionHand.OFF_HAND);
        HumanoidArm poseMainArm = hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
        MeleeAnimationManager.applyWindupPose(skeleton, entityIn, MKNpcMeleeAnimations.GOLEM_DEFAULT,
                hand, MKNpcMeleeAnimations.GOLEM_FAMILY, entityIn.getCurrentMeleeWindupVariant(hand),
                ModelPoseAnimator.Context.windup(windupProgress, poseMainArm, hand, dualWielding));
    }

    private void applyDefaultStrikePose(T entityIn, InteractionHand hand, float swing, float ageInTicks) {
        HumanoidArm poseMainArm = hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
        MeleeAnimationManager.applyStrikePose(skeleton, entityIn, MKNpcMeleeAnimations.GOLEM_DEFAULT,
                hand, MKNpcMeleeAnimations.GOLEM_FAMILY, entityIn.getCurrentStrikePoseIndex(hand),
                ModelPoseAnimator.Context.strike(swing, ageInTicks, poseMainArm, hand, false));
    }

    private void resetUpperBodyPose() {
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;
        this.rightArm.xRot = 0.0F;
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.xRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.leftArm.zRot = 0.0F;
    }
}
