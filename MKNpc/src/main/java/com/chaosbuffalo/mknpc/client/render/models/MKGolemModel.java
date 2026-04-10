package com.chaosbuffalo.mknpc.client.render.models;


import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

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
        float swingProgress = entityIn.getVisualMeleeAttackAnim(partialTick);
        if (swingProgress > 0) {
            applyAttackVariant(entityIn, swingProgress);
        } else if (entityIn.getMeleeWindupProgress(partialTick) > 0.0F) {
            resetUpperBodyPose();
            float windupProgress = entityIn.getMeleeWindupProgress(partialTick);
            float windupPose = Mth.sin(windupProgress * ((float) Math.PI / 2.0F));
            applyWindupVariant(entityIn, windupPose);
        } else {
            resetUpperBodyPose();
            rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;
            leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(limbSwing, 13.0F)) * limbSwingAmount;

        }

    }

    private void applyWindupVariant(T entityIn, float windupPose) {
        int swingVariant = Math.floorMod(entityIn.getCurrentLocalSwingVariant(), 3);
        switch (swingVariant) {
            case 1 -> applyRoundhouseWindup(this.leftArm, this.rightArm, windupPose, 1.0F);
            case 2 -> applyDoubleArmSmashWindup(windupPose);
            default -> applyRoundhouseWindup(this.rightArm, this.leftArm, windupPose, -1.0F);
        }
    }

    private void applyAttackVariant(T entityIn, float swingProgress) {
        resetUpperBodyPose();

        int swingVariant = Math.floorMod(entityIn.getCurrentLocalSwingVariant() - 1, 3);
        switch (swingVariant) {
            case 1 -> applyRoundhousePunch(this.leftArm, this.rightArm, swingProgress, 1.0F);
            case 2 -> applyDoubleArmSmash(swingProgress);
            default -> applyRoundhousePunch(this.rightArm, this.leftArm, swingProgress, -1.0F);
        }
    }

    private void applyRoundhouseWindup(ModelPart strikingArm, ModelPart counterArm, float windupPose, float sideSign) {
        this.body.yRot = sideSign * -0.34F * windupPose;
        this.body.xRot = -0.08F * windupPose;

        strikingArm.xRot = Mth.lerp(windupPose, 0.0F, -2.15F);
        strikingArm.yRot = sideSign * -1.05F * windupPose;
        strikingArm.zRot = sideSign * 0.28F * windupPose;

        counterArm.xRot = Mth.lerp(windupPose, 0.0F, -0.4F);
        counterArm.yRot = sideSign * 0.35F * windupPose;
        counterArm.zRot = sideSign * 0.08F * windupPose;
    }

    private void applyDoubleArmSmashWindup(float windupPose) {
        this.body.xRot = -0.2F * windupPose;

        this.rightArm.xRot = Mth.lerp(windupPose, 0.0F, -2.25F);
        this.leftArm.xRot = Mth.lerp(windupPose, 0.0F, -2.25F);
        this.rightArm.yRot = 0.75F * windupPose;
        this.leftArm.yRot = -0.75F * windupPose;
        this.rightArm.zRot = 0.2F * windupPose;
        this.leftArm.zRot = -0.2F * windupPose;
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

    private void applyRoundhousePunch(ModelPart strikingArm, ModelPart counterArm, float swingProgress, float sideSign) {
        float swing = Mth.clamp(swingProgress, 0.0F, 1.0F);
        float wind = 1.0F - swing;
        float strike = Mth.sin(swing * (float) Math.PI);
        float followThrough = Mth.sin(Mth.clamp((swing - 0.45F) / 0.55F, 0.0F, 1.0F) * ((float) Math.PI / 2.0F));

        this.body.yRot = sideSign * (-0.32F * wind + 0.5F * strike + 0.34F * followThrough);
        this.body.xRot = 0.06F * strike;

        strikingArm.xRot = -1.65F - 0.55F * wind + 0.75F * strike + 0.35F * followThrough;
        strikingArm.yRot = sideSign * (-1.05F * wind + 1.25F * strike + 0.85F * followThrough);
        strikingArm.zRot = sideSign * (-0.25F - 0.35F * strike + 0.1F * followThrough);

        counterArm.xRot = -0.15F - 0.1F * strike;
        counterArm.yRot = -sideSign * (0.35F + 0.15F * strike);
        counterArm.zRot = -sideSign * 0.12F;
    }

    private void applyDoubleArmSmash(float swingProgress) {
        float swing = Mth.clamp(swingProgress, 0.0F, 1.0F);
        float wind = 1.0F - swing;
        float strike = Mth.sin(swing * (float) Math.PI);
        float followThrough = Mth.sin(Mth.clamp((swing - 0.35F) / 0.35F, 0.0F, 1.0F) * ((float) Math.PI / 2.0F));

        this.body.xRot = -0.2F * wind + 0.12F * strike + 0.08F * followThrough;

        this.rightArm.xRot = -2.25F * wind - 0.75F * strike - 0.85F * followThrough;
        this.leftArm.xRot = -2.25F * wind - 0.75F * strike - 0.85F * followThrough;
        this.rightArm.yRot = 0.75F * wind - 0.18F * strike - 0.28F * followThrough;
        this.leftArm.yRot = -0.75F * wind + 0.18F * strike + 0.28F * followThrough;
        this.rightArm.zRot = 0.2F * wind - 0.05F * strike - 0.08F * followThrough;
        this.leftArm.zRot = -0.2F * wind + 0.05F * strike + 0.08F * followThrough;
    }
}
