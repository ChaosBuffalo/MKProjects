package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;

public class MKSkeletalModel<T extends MKEntity> extends MKBipedModel<T> {

    public MKSkeletalModel(ModelPart part) {
        super(part);
    }

    public static MeshDefinition createBodyLayer(ModelArgs args) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(args.deformation, args.heightOffset);
        PartDefinition partdefinition = meshdefinition.getRoot();
        if (args.overrideBaseParts) {
            partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, args.deformation), PartPose.offset(-5.0F, 2.0F, 0.0F));
            partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, args.deformation), PartPose.offset(5.0F, 2.0F, 0.0F));
            partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, args.deformation), PartPose.offset(-2.0F, 12.0F, 0.0F));
            partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, args.deformation), PartPose.offset(2.0F, 12.0F, 0.0F));
        }
        return meshdefinition;
    }

    public void translateToHand(HumanoidArm sideIn, PoseStack matrixStackIn) {
        float f = sideIn == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ModelPart modelpart = this.getArm(sideIn);
        modelpart.x += f;
        modelpart.translateAndRotate(matrixStackIn);
        modelpart.x -= f;
    }
}
