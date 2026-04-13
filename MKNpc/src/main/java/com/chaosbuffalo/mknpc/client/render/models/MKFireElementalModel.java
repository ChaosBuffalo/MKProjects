package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mkcore.core.combat.MKMeleeManager;
import com.chaosbuffalo.mknpc.client.render.animations.MKNpcMeleeAnimations;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class MKFireElementalModel<T extends MKEntity> extends MKBipedModel<T> {
    private final ModelPart root;
    private final ModelPart vortexTop;
    private final ModelPart vortexMid;
    private final ModelPart vortexBottom;

    public MKFireElementalModel(ModelPart modelPart) {
        super(modelPart);
        this.root = modelPart;
        this.vortexTop = this.root.getChild("vortex_top");
        this.vortexMid = this.vortexTop.getChild("vortex_mid");
        this.vortexBottom = this.vortexMid.getChild("vortex_bottom");
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
    }

    public static MeshDefinition createBodyLayer(ModelArgs args) {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(args.deformation, args.heightOffset);
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition head = root.getChild("head");
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48)
                        .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, args.deformation),
                PartPose.offset(5.0F, 2.5F, 0.0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, args.deformation),
                PartPose.offset(-5.0F, 2.5F, 0.0F));
        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(96, 0)
                        .addBox(0.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, args.deformation),
                PartPose.offsetAndRotation(2.1F, -7.5F, 2.0F, 0.15F, 1.0F, 0.78F));
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(96, 10)
                        .mirror()
                        .addBox(-6.0F, -8.0F, 0.0F, 6.0F, 10.0F, 0.0F, args.deformation),
                PartPose.offsetAndRotation(-2.1F, -7.5F, 2.0F, 0.15F, -1.0F, -0.78F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition vortexTop = root.addOrReplaceChild("vortex_top",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-4.0F, 0.0F, -4.0F, 8.0F, 4.0F, 8.0F, args.deformation),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition vortexMid = vortexTop.addOrReplaceChild("vortex_mid",
                CubeListBuilder.create().texOffs(32, 32)
                        .addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F, args.deformation),
                PartPose.offset(0.0F, 4.0F, 0.0F));
        vortexMid.addOrReplaceChild("vortex_bottom",
                CubeListBuilder.create().texOffs(64, 32)
                        .addBox(-8.0F, 0.0F, -8.0F, 16.0F, 4.0F, 16.0F, args.deformation),
                PartPose.offset(0.0F, 4.0F, 0.0F));
        return meshDefinition;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(vortexTop));
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
        this.rightLeg.xRot = 0.0F;
        this.leftLeg.xRot = 0.0F;

        float walkAmount = Math.min(limbSwingAmount, 1.0F);
        float swirlTime = ageInTicks * 0.18F;
        float swayTime = limbSwing * 0.55F;
        float sway = walkAmount * 1.6F;
        float forwardWave = swirlTime * 1.1F + swayTime;

        animateBand(vortexTop, swirlTime * 1.7F + swayTime * 0.9F, sway, 0.12F, 0.9F,
                Mth.sin(forwardWave - 0.55F) * sway * 0.18F,
                Mth.sin(forwardWave + 0.35F) * sway * 0.9F);
        animateBand(vortexMid, -swirlTime * 2.6F - swayTime * 1.15F, sway, 0.18F, 1.4F,
                Mth.sin(forwardWave - 0.1F) * sway * 0.1F,
                Mth.sin(forwardWave + 1.0F) * sway * 1.45F);
        animateBand(vortexBottom, swirlTime * 0.8F + swayTime * 0.3F, sway, 0.24F, 1.8F,
                Mth.sin(forwardWave + 0.45F) * sway * 0.06F,
                Mth.sin(forwardWave + 1.7F) * sway * 0.55F);

        this.body.z += Mth.sin(forwardWave + 0.55F) * sway * 0.1F;
        this.body.x += Mth.sin(forwardWave - 0.15F) * sway * 0.03F;
        this.body.xRot += Mth.sin(forwardWave + 0.9F) * walkAmount * 0.08F;
        this.body.zRot += Mth.sin(forwardWave + 0.2F) * walkAmount * 0.06F;
        this.body.yRot += Mth.sin(forwardWave + 1.0F) * walkAmount * 0.04F;
        this.rightArm.zRot += this.body.zRot * 0.35F;
        this.leftArm.zRot += this.body.zRot * 0.35F;
        this.rightArm.xRot += this.body.xRot * 0.2F;
        this.leftArm.xRot += this.body.xRot * 0.2F;
        this.body.y += Mth.sin(swirlTime * 1.4F) * walkAmount * 0.22F;
        this.head.y += Mth.sin(swirlTime * 1.4F + 0.35F) * walkAmount * 0.08F;
        this.head.xRot += this.body.xRot * 0.35F;
        this.head.zRot += this.body.zRot * 0.2F;
    }

    private static void animateBand(ModelPart band, float yRot, float sway, float tilt, float phaseOffset, float baseX, float baseZ) {
        float wave = yRot + phaseOffset;
        band.x = baseX + Mth.sin(wave) * sway;
        band.z = baseZ + Mth.cos(wave * 0.85F) * sway * 0.55F;
        band.yRot = yRot;
        band.xRot = Mth.cos(wave * 1.15F) * tilt * sway;
        band.zRot = Mth.sin(wave * 0.95F) * tilt * sway * 0.8F;
    }

    @Override
    protected boolean applyHeavyMeleeSwing(T entityIn, InteractionHand hand, float swing, float ageInTicks) {
        if (super.applyHeavyMeleeSwing(entityIn, hand, swing, ageInTicks)) {
            return true;
        }
        applyDefaultStrikePose(entityIn, hand, swing, ageInTicks);
        return true;
    }

    @Override
    protected void applyMeleeWindupPose(T entityIn, InteractionHand hand, float windupProgress) {
        boolean dualWielding = MKMeleeManager.canUseForAttack(entityIn, InteractionHand.MAIN_HAND) &&
                MKMeleeManager.canUseForAttack(entityIn, InteractionHand.OFF_HAND);
        HumanoidArm poseMainArm = hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
        boolean applied = MeleeAnimationManager.applyResolvedWindupPose(skeleton, entityIn, hand,
                MeleeAnimationManager.BIPED_FAMILY, entityIn.getCurrentMeleeWindupVariant(hand),
                ModelPoseAnimator.Context.windup(windupProgress, poseMainArm, hand, dualWielding));
        if (!applied) {
            MeleeAnimationManager.applyWindupPose(skeleton, entityIn, MKNpcMeleeAnimations.GOLEM_DEFAULT,
                    hand, MKNpcMeleeAnimations.GOLEM_FAMILY, entityIn.getCurrentMeleeWindupVariant(hand),
                    ModelPoseAnimator.Context.windup(windupProgress, poseMainArm, hand, dualWielding));
        }
    }

    private void applyDefaultStrikePose(T entityIn, InteractionHand hand, float swing, float ageInTicks) {
        HumanoidArm poseMainArm = hand == InteractionHand.MAIN_HAND ? entityIn.getMainArm() : entityIn.getMainArm().getOpposite();
        MeleeAnimationManager.applyStrikePose(skeleton, entityIn, MKNpcMeleeAnimations.GOLEM_DEFAULT,
                hand, MKNpcMeleeAnimations.GOLEM_FAMILY, entityIn.getCurrentStrikePoseIndex(hand),
                ModelPoseAnimator.Context.strike(swing, ageInTicks, poseMainArm, hand, false));
    }

    @Override
    public void translateToHand(HumanoidArm sideIn, PoseStack matrixStackIn) {
        ModelPart arm = this.getArm(sideIn);
        float armOffset = 0.5F * (sideIn == HumanoidArm.RIGHT ? 1.0F : -1.0F);
        arm.x += armOffset;
        arm.translateAndRotate(matrixStackIn);
        arm.x -= armOffset;
    }
}
