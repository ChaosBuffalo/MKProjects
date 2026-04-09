package com.chaosbuffalo.mknpc.client.render.models;


import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mknpc.client.render.animations.MKNpcMeleeAnimations;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;

public class MKGolemModel<T extends MKEntity> extends MKBipedModel<T> {
    public MKGolemModel(ModelPart modelPart) {
        super(modelPart);
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

        AdditionalBipedAnimation<MKEntity> animation = getAdditionalAnimation(entityIn);
        if (animation != null) {
            animation.apply(entityIn);
        }
    }

    @Override
    public void prepareMobModel(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        float swingProgress = entityIn.getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTick);
        if (entityIn.hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTick)) {
            applyAttackPose(entityIn, swingProgress, partialTick);
        } else if (entityIn.getMeleeWindupProgress(partialTick) > 0.0F) {
            resetUpperBodyPose();
            float windupProgress = entityIn.getMeleeWindupProgress(partialTick);
            applyWindupPose(entityIn, windupProgress);
        } else {
            resetUpperBodyPose();
            rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
            leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;

        }

    }

    private void applyWindupPose(T entityIn, float windupProgress) {
        MeleeAnimationManager.applyWindupPose(skeleton, entityIn, MKNpcMeleeAnimations.GOLEM_DEFAULT,
                InteractionHand.MAIN_HAND, MKNpcMeleeAnimations.GOLEM_FAMILY, entityIn.getCurrentMeleeWindupVariant(),
                ModelPoseAnimator.Context.windup(windupProgress, entityIn.getMainArm(), InteractionHand.MAIN_HAND));
    }

    private void applyAttackPose(T entityIn, float swingProgress, float partialTick) {
        resetUpperBodyPose();
        MeleeAnimationManager.applyStrikePose(skeleton, entityIn, MKNpcMeleeAnimations.GOLEM_DEFAULT,
                InteractionHand.MAIN_HAND, MKNpcMeleeAnimations.GOLEM_FAMILY,
                entityIn.getCurrentStrikePoseIndex(InteractionHand.MAIN_HAND),
                ModelPoseAnimator.Context.strike(swingProgress, partialTick, entityIn.getMainArm(), InteractionHand.MAIN_HAND));
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
