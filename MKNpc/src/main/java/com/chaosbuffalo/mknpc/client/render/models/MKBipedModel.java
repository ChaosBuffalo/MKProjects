package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedCastAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedStunAnimation;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mknpc.client.render.animations.MKEntityCompleteCastAnimation;
import com.chaosbuffalo.mknpc.client.render.models.styling.ModelArgs;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.AnimationUtils;
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
        if (this.attackTime <= 0.0F) {
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
        if (this.attackTime > 0.0F && (itemstack.isEmpty() || !(itemstack.getItem() instanceof BowItem))) {
            applyHeavyMeleeSwing(entityIn, ageInTicks);
        } else {
            super.setupAttackAnimation(entityIn, ageInTicks);
        }
    }

    protected void applyHeavyMeleeSwing(T entityIn, float ageInTicks) {
        HumanoidArm mainArm = entityIn.getMainArm();
        ModelPart weaponArm = mainArm == HumanoidArm.RIGHT ? this.rightArm : this.leftArm;
        ModelPart offArm = mainArm == HumanoidArm.RIGHT ? this.leftArm : this.rightArm;
        float swing = this.attackTime;
        float swingSin = Mth.sin(swing * (float) Math.PI);
        float impactCurve = Mth.sin((1.0F - (1.0F - swing) * (1.0F - swing)) * (float) Math.PI);
        float followThrough = Mth.sin(Mth.clamp((swing - 0.45F) / 0.55F, 0.0F, 1.0F) * ((float) Math.PI / 2.0F));
        float handedness = mainArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        int swingVariant = Math.floorMod(entityIn.getCurrentLocalSwingVariant(), 3);
        float bodyYawBase;
        float bodyYawSwing;
        float bodyYawFollow;
        float weaponYawBase;
        float weaponYawSwing;
        float weaponYawFollow;
        float weaponPitchBase;
        float weaponPitchSwing;
        float weaponPitchImpact;
        float weaponPitchFollow;
        float weaponRollBase;
        float weaponRollSwing;
        float weaponRollFollow;
        float offArmYawBase;
        float offArmYawSwing;
        float offArmPitchBase;
        float offArmPitchSwing;
        float offArmPitchImpact;
        float offArmRollBase;
        float offArmRollSwing;
        float headPitchSwing;
        float headPitchFollow;
        float headYawFollowScale;
        float weaponRollDirection;
        float offArmRollDirection;
        float swingDirection;

        switch (swingVariant) {
            case 1 -> {
                bodyYawBase = 0.03F;
                bodyYawSwing = 0.05F;
                bodyYawFollow = 0.04F;
                weaponYawBase = 0.04F;
                weaponYawSwing = 0.06F;
                weaponYawFollow = 0.04F;
                weaponPitchBase = -2.3F;
                weaponPitchSwing = 1.0F;
                weaponPitchImpact = 0.75F;
                weaponPitchFollow = 2.85F;
                weaponRollBase = 0.08F;
                weaponRollSwing = 0.06F;
                weaponRollFollow = 0.06F;
                offArmYawBase = 0.05F;
                offArmYawSwing = 0.08F;
                offArmPitchBase = -1.05F;
                offArmPitchSwing = 0.05F;
                offArmPitchImpact = 0.04F;
                offArmRollBase = 0.05F;
                offArmRollSwing = 0.04F;
                headPitchSwing = 0.04F;
                headPitchFollow = 0.1F;
                headYawFollowScale = 0.12F;
                weaponRollDirection = 1.0F;
                offArmRollDirection = -1.0F;
                swingDirection = 1.0F;
            }
            case 2 -> {
                bodyYawBase = 0.12F;
                bodyYawSwing = 0.16F;
                bodyYawFollow = 0.11F;
                weaponYawBase = 0.24F;
                weaponYawSwing = 0.22F;
                weaponYawFollow = 0.12F;
                weaponPitchBase = -1.7F;
                weaponPitchSwing = 0.85F;
                weaponPitchImpact = 0.55F;
                weaponPitchFollow = 2.45F;
                weaponRollBase = 0.4F;
                weaponRollSwing = 0.3F;
                weaponRollFollow = 0.18F;
                offArmYawBase = 0.1F;
                offArmYawSwing = 0.12F;
                offArmPitchBase = -0.9F;
                offArmPitchSwing = 0.08F;
                offArmPitchImpact = 0.06F;
                offArmRollBase = 0.08F;
                offArmRollSwing = 0.06F;
                headPitchSwing = 0.06F;
                headPitchFollow = 0.08F;
                headYawFollowScale = 0.24F;
                weaponRollDirection = -1.0F;
                offArmRollDirection = 1.0F;
                swingDirection = -1.0F;
            }
            default -> {
                bodyYawBase = 0.12F;
                bodyYawSwing = 0.16F;
                bodyYawFollow = 0.11F;
                weaponYawBase = 0.24F;
                weaponYawSwing = 0.22F;
                weaponYawFollow = 0.12F;
                weaponPitchBase = -1.7F;
                weaponPitchSwing = 0.85F;
                weaponPitchImpact = 0.55F;
                weaponPitchFollow = 2.45F;
                weaponRollBase = 0.4F;
                weaponRollSwing = 0.3F;
                weaponRollFollow = 0.18F;
                offArmYawBase = 0.08F;
                offArmYawSwing = 0.1F;
                offArmPitchBase = -0.95F;
                offArmPitchSwing = 0.1F;
                offArmPitchImpact = 0.08F;
                offArmRollBase = 0.06F;
                offArmRollSwing = 0.06F;
                headPitchSwing = 0.06F;
                headPitchFollow = 0.08F;
                headYawFollowScale = 0.2F;
                weaponRollDirection = 1.0F;
                offArmRollDirection = -1.0F;
                swingDirection = 1.0F;
            }
        }

        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;

        this.body.yRot = -handedness * swingDirection * (bodyYawBase + swingSin * bodyYawSwing + followThrough * bodyYawFollow);
        weaponArm.yRot = -handedness * swingDirection * (weaponYawBase + swingSin * weaponYawSwing + followThrough * weaponYawFollow);
        offArm.yRot = handedness * swingDirection * (offArmYawBase + swingSin * offArmYawSwing);

        weaponArm.xRot = weaponPitchBase - swingSin * weaponPitchSwing - impactCurve * weaponPitchImpact + followThrough * weaponPitchFollow;
        offArm.xRot = offArmPitchBase + swingSin * offArmPitchSwing - impactCurve * offArmPitchImpact;

        weaponArm.zRot = handedness * weaponRollDirection * (weaponRollBase + swingSin * weaponRollSwing + followThrough * weaponRollFollow);
        offArm.zRot = handedness * offArmRollDirection * (offArmRollBase + swingSin * offArmRollSwing);

        this.head.xRot += swingSin * headPitchSwing + followThrough * headPitchFollow;
        this.head.yRot += this.body.yRot * headYawFollowScale;

        AnimationUtils.bobArms(this.rightArm, this.leftArm, ageInTicks);
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
