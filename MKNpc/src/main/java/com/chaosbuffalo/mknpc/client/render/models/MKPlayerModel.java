package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

import java.util.List;
import java.util.Random;

public class MKPlayerModel<T extends MKEntity> extends MKBipedModel<T> {
    private final List<ModelPart> modelRenderers;
    public final ModelPart bipedLeftArmwear;
    public final ModelPart bipedRightArmwear;
    public final ModelPart bipedLeftLegwear;
    public final ModelPart bipedRightLegwear;
    public final ModelPart bipedBodyWear;
    private final boolean slim;

    public static MeshDefinition createMesh(CubeDeformation p_170826_, boolean doSmallArms) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(p_170826_, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, p_170826_), PartPose.ZERO);
        partdefinition.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, p_170826_, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        float f = 0.25F;
        if (doSmallArms) {
            partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
        } else {
            partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.0F, 0.0F));
            partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
            partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        }

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.ZERO);
        return meshdefinition;
    }

    public MKPlayerModel(ModelPart modelPart, boolean slim) {
        super(modelPart);
        this.bipedLeftArmwear = modelPart.getChild("left_sleeve");
        this.bipedRightArmwear = modelPart.getChild("right_sleeve");
        this.bipedLeftLegwear = modelPart.getChild("left_pants");
        this.bipedRightLegwear = modelPart.getChild("right_pants");
        this.bipedBodyWear = modelPart.getChild("jacket");
        this.modelRenderers = modelPart.getAllParts().filter((part) -> !part.isEmpty()).collect(
                ImmutableList.toImmutableList());
        this.slim = slim;
    }

    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);
        this.bipedLeftArmwear.visible = visible;
        this.bipedRightArmwear.visible = visible;
        this.bipedLeftLegwear.visible = visible;
        this.bipedRightLegwear.visible = visible;
        this.bipedBodyWear.visible = visible;
    }

    protected Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.bipedLeftLegwear,
                this.bipedRightLegwear, this.bipedLeftArmwear, this.bipedRightArmwear, this.bipedBodyWear));
    }

    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bipedLeftLegwear.copyFrom(this.leftLeg);
        this.bipedRightLegwear.copyFrom(this.rightLeg);
        this.bipedLeftArmwear.copyFrom(this.leftArm);
        this.bipedRightArmwear.copyFrom(this.rightArm);
        this.bipedBodyWear.copyFrom(this.body);
    }

    public void translateToHand(HumanoidArm sideIn, PoseStack matrixStackIn) {
        ModelPart modelrenderer = this.getArm(sideIn);
        if (this.slim) {
            float f = 0.5F * (float)(sideIn == HumanoidArm.RIGHT ? 1 : -1);
            modelrenderer.x += f;
            modelrenderer.translateAndRotate(matrixStackIn);
            modelrenderer.x -= f;
        } else {
            modelrenderer.translateAndRotate(matrixStackIn);
        }

    }

    public ModelPart getRandomModelRenderer(Random randomIn) {
        return this.modelRenderers.get(randomIn.nextInt(this.modelRenderers.size()));
    }

//
//    @Override
//    public void accept(ModelPart rendererIn) {
//        if (this.modelRenderers == null) {
//            this.modelRenderers = Lists.newArrayList();
//        }
//        this.modelRenderers.add(rendererIn);
//    }
}
