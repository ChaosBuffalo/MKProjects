package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedMeleeSwingAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedCastAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedStunAnimation;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mknpc.client.render.animations.MKEntityCompleteCastAnimation;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class MKBipedModel<T extends MKEntity> extends HumanoidModel<T> {
    private final BipedCastAnimation<MKEntity> castAnimation = new BipedCastAnimation<>(this);
    private final MKEntityCompleteCastAnimation completeCastAnimation = new MKEntityCompleteCastAnimation(this);
    private final BipedStunAnimation<MKEntity> stunAnimation = new BipedStunAnimation<>(this);


    public MKBipedModel(ModelPart modelPart) {
        super(modelPart);
    }

    public MKBipedModel(ModelPart modelPart, Function<ResourceLocation, RenderType> renderSupplier) {
        super(modelPart, renderSupplier);
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
        if (this.attackTime <= 0.0F && entityIn.getVisualMeleeAttackAnim(ageInTicks - entityIn.tickCount) <= 0.0F) {
            float windupProgress = entityIn.getMeleeWindupProgress(ageInTicks - entityIn.tickCount);
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
        float swing = entityIn.getVisualMeleeAttackAnim(ageInTicks - entityIn.tickCount);
        if (swing > 0.0F && (itemstack.isEmpty() || !(itemstack.getItem() instanceof BowItem))) {
            applyHeavyMeleeSwing(entityIn, swing, ageInTicks);
        } else {
            super.setupAttackAnimation(entityIn, ageInTicks);
        }
    }

    protected void applyHeavyMeleeSwing(T entityIn, float swing, float ageInTicks) {
        BipedMeleeSwingAnimation.apply(this, entityIn.getMainArm(), swing,
                entityIn.getCurrentLocalSwingVariant(), ageInTicks);
    }

    protected void applyMeleeWindupPose(T entityIn, float windupProgress) {
        HumanoidArm mainArm = entityIn.getMainArm();
        ModelPart mainWeaponArm = mainArm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        ModelPart offArm = mainArm == HumanoidArm.RIGHT ? this.leftArm : this.rightArm;
        float windupPose = Mth.sin(windupProgress * ((float) Math.PI / 2.0F));
        float handedness = mainArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float mainArmRestPitch = -0.25F;
        float offArmRestPitch = -0.25F;
        float mainArmPitch = -3.0F;
        float offArmPitch = -2.0F;
        float mainArmYaw = handedness * 0.6F;
        float offArmYaw = handedness * 0.4F;

        mainWeaponArm.xRot = Mth.lerp(windupPose, mainArmRestPitch, mainArmPitch);
        offArm.xRot = Mth.lerp(windupPose, offArmRestPitch, offArmPitch);
        mainWeaponArm.yRot = Mth.lerp(windupPose, 0.0F, mainArmYaw);
        offArm.yRot = Mth.lerp(windupPose, 0.0F, offArmYaw);
        mainWeaponArm.zRot = 0.0F;
        offArm.zRot = Mth.lerp(windupPose, 0.0F, -handedness * 0.08F);
        this.body.xRot -= windupPose * 0.12F;
        this.body.yRot = Mth.lerp(windupPose, 0.0F, handedness * 0.05F);
        this.head.xRot += windupPose * 0.1F;
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
