package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mknpc.client.render.animations.MKNpcMeleeAnimations;
import com.chaosbuffalo.mknpc.client.render.skeleton.SkullSkeleton;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class MKSkullModel<T extends MKEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    protected final ModelPart head;
    protected final ModelPart jaw;
    private final SkullSkeleton skeleton;

    public MKSkullModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.skeleton = new SkullSkeleton(this.head, this.jaw);
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 7.0F, 8.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        return meshDefinition;
    }

    public static LayerDefinition createMobHeadLayer() {
        return LayerDefinition.create(createHeadModel(), 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTicks = ageInTicks - entity.tickCount;
        float attackAnim = entity.getVisualMeleeAttackAnim(InteractionHand.MAIN_HAND, partialTicks);
        boolean attackActive = entity.hasActiveVisualMeleeAttack(InteractionHand.MAIN_HAND, partialTicks);
        float windupProgress = entity.getMeleeWindupProgress(partialTicks);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);
        this.head.y = 20.0F;
        this.head.z = 0.0F;
        this.jaw.xRot = 0.0F;

        if (attackActive) {
            MeleeAnimationManager.applyStrikePose(skeleton, entity, MKNpcMeleeAnimations.SKULL_DEFAULT,
                    InteractionHand.MAIN_HAND, MKNpcMeleeAnimations.SKULL_FAMILY,
                    entity.getCurrentStrikePoseIndex(InteractionHand.MAIN_HAND),
                    ModelPoseAnimator.Context.strike(attackAnim, ageInTicks, netHeadYaw, headPitch, HumanoidArm.RIGHT,
                            InteractionHand.MAIN_HAND));
        } else if (windupProgress > 0.0F) {
            MeleeAnimationManager.applyWindupPose(skeleton, entity, MKNpcMeleeAnimations.SKULL_DEFAULT,
                    InteractionHand.MAIN_HAND, MKNpcMeleeAnimations.SKULL_FAMILY,
                    entity.getCurrentStrikePoseIndex(InteractionHand.MAIN_HAND),
                    ModelPoseAnimator.Context.windup(windupProgress, ageInTicks, netHeadYaw, headPitch, HumanoidArm.RIGHT,
                            InteractionHand.MAIN_HAND));
        } else {
            MeleeAnimationPose pose = MeleeAnimationManager.getPose(MKNpcMeleeAnimations.SKULL_IDLE);
            if (pose != null) {
                ModelPoseAnimator.apply(skeleton, MKNpcMeleeAnimations.SKULL_FAMILY, pose,
                        ModelPoseAnimator.Context.idle(ageInTicks, netHeadYaw, headPitch, HumanoidArm.RIGHT));
            }
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
