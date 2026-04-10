package com.chaosbuffalo.mkcore.client.rendering.model;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.AdditionalBipedAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedCastAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.BipedStunAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.PlayerCompleteCastAnimation;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public class MKPlayerModel extends PlayerModel<AbstractClientPlayer> {
    private final BipedCastAnimation<Player> castAnimation = new BipedCastAnimation<>(this);
    private final PlayerCompleteCastAnimation completeCastAnimation = new PlayerCompleteCastAnimation(this);
    private final BipedStunAnimation<Player> stunAnimation = new BipedStunAnimation<>(this);
    private final BipedSkeleton<AbstractClientPlayer, MKPlayerModel> skeleton;

    public MKPlayerModel(ModelPart p_170821_, boolean p_170822_) {
        super(p_170821_, p_170822_);
        this.skeleton = new BipedSkeleton<>(this);
    }

    @Override
    public void setupAnim(AbstractClientPlayer entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        MKCore.getPlayer(entityIn).ifPresent(mkEntityData -> {
            this.head.zRot = 0.0f;
            AdditionalBipedAnimation<Player> animation = getAdditionalAnimation(mkEntityData);
            if (animation != null) {
                animation.apply(entityIn);
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
            float visualSwing = playerData.getCombatExtension().getVisualMeleeAttackAnim(ageInTicks - entityIn.tickCount);
            float swing = visualSwing > 0.0F ? visualSwing : this.attackTime;
            if (swing > 0.0F) {
                int variant = playerData.getCombatExtension().hasVisualMeleeAttackSequence() ?
                        playerData.getCombatExtension().getCurrentStrikePoseIndex() :
                        playerData.getCombatExtension().getCurrentPrimarySwingVariant();
                if (!applyMeleeAnimationPose(entityIn, swing, ageInTicks, variant)) {
                    super.setupAttackAnimation(entityIn, ageInTicks);
                }
            } else {
                super.setupAttackAnimation(entityIn, ageInTicks);
            }
        }, () -> super.setupAttackAnimation(entityIn, ageInTicks));
    }

    private boolean applyMeleeAnimationPose(AbstractClientPlayer entityIn, float swing, float ageInTicks, int variant) {
        return MeleeAnimationManager.applyResolvedStrikePose(skeleton, entityIn, MeleeAnimationManager.BIPED_FAMILY, variant,
                ModelPoseAnimator.Context.strike(swing, ageInTicks, entityIn.getMainArm()));
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color) {
        super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);

    }

    public AdditionalBipedAnimation<Player> getAdditionalAnimation(MKPlayerData playerData) {
        if (playerData.getEffects().isEffectActive(CoreEffects.STUN.get())) {
            return stunAnimation;
        }
        switch (playerData.getAnimationModule().getPlayerVisualCastState()) {
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
