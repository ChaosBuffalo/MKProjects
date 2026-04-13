package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.rendering.model.MKPlayerModel;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.core.EntityAnimationModule;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.HeldItemParticleEffectInstance;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;


public class MKPlayerRenderer extends PlayerRenderer {
    private final BipedSkeleton<AbstractClientPlayer, MKPlayerModel> skeleton;

    public MKPlayerRenderer(EntityRendererProvider.Context context, boolean useSmallArms) {
        super(context, useSmallArms);
        this.model = new MKPlayerModel(context.bakeLayer(useSmallArms ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), useSmallArms);
        this.skeleton = new BipedSkeleton<>((MKPlayerModel) model);
    }

    @Override
    public void render(AbstractClientPlayer entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        MKCore.getPlayer(entityIn).ifPresent(data -> {
            EntityAnimationModule.VisualCastState state = data.getAnimationModule().getVisualCastState();
            if (state == EntityAnimationModule.VisualCastState.CASTING || state == EntityAnimationModule.VisualCastState.RELEASE) {
                MKAbility ability = data.getAnimationModule().getCastingAbility();
                if (ability != null) {
                    // do spell casting
                    if (ability.hasCastingParticles()) {
                        ParticleAnimation anim = ParticleAnimationManager.getAnimation(ability.getCastingParticles());
                        if (anim != null) {
                            float scale = MathUtils.lerp(.6f, 1.f, data.getAnimationModule().getCastRatio());
                            Vec3 scaleVec = new Vec3(scale, scale, scale);
                            Optional<Vec3> leftPos = getHandPosition(partialTicks, entityIn, HumanoidArm.LEFT);
                            leftPos.ifPresent(x -> anim.spawn(entityIn.getCommandSenderWorld(), x, scaleVec, null));
                            Optional<Vec3> rightPos = getHandPosition(partialTicks, entityIn, HumanoidArm.RIGHT);
                            rightPos.ifPresent(x -> anim.spawn(entityIn.getCommandSenderWorld(), x, scaleVec, null));
                        }
                    }
                }
            }
            data.getAnimationModule().getParticleInstances().forEach(instance -> {
                instance.update(entityIn, skeleton, partialTicks, getRenderOffset(entityIn, partialTicks));
            });
        });
    }

    public void renderHandFirstPerson(AbstractClientPlayer playerIn) {
        MKCore.getPlayer(playerIn).ifPresent(data -> {
            EntityAnimationModule.VisualCastState state = data.getAnimationModule().getVisualCastState();
            if (state == EntityAnimationModule.VisualCastState.CASTING || state == EntityAnimationModule.VisualCastState.RELEASE) {
                MKAbility ability = data.getAnimationModule().getCastingAbility();
                if (ability != null) {
                    // do spell casting
                    if (ability.hasCastingParticles()) {

                        ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(ability.getCastingParticles());
                        if (anim != null) {
                            float scale = MathUtils.lerp(.25f, .8f, data.getAnimationModule().getCastRatio());
                            Vec3 scaleVec = new Vec3(scale, scale, scale);
                            float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                            Vec3 leftPos = getFirstPersonHandPosition(HumanoidArm.LEFT,
                                    (LocalPlayer) playerIn, data.getAnimationModule(), partialTicks);
                            anim.spawn(playerIn.getCommandSenderWorld(), leftPos, scaleVec, null);
                            Vec3 rightPos = getFirstPersonHandPosition(HumanoidArm.RIGHT,
                                    (LocalPlayer) playerIn, data.getAnimationModule(), partialTicks);
                            anim.spawn(playerIn.getCommandSenderWorld(), rightPos, scaleVec, null );
                        }
                    }
                }
            }


            data.getAnimationModule().getParticleInstances().forEach(instance -> {
                if (!(instance instanceof HeldItemParticleEffectInstance)) {
                    instance.update(playerIn, skeleton, 0.0f, getRenderOffset(playerIn, 0.0f));
                }
            });
        });
    }

    private Vec3 getFirstPersonHandPosition(HumanoidArm handSide, LocalPlayer playerEntityIn,
                                            EntityAnimationModule animationModule, float partialTicks) {
        Vec3 eyePos = playerEntityIn.getEyePosition(partialTicks);
        Vec3 look = playerEntityIn.getViewVector(partialTicks).normalize();
        Vec3 up = playerEntityIn.getUpVector(partialTicks).normalize();
        Vec3 right = look.cross(up).normalize();
        double handScalar = handSide == HumanoidArm.RIGHT ? 1.0 : -1.0;
        Vec3 localOffset = new Vec3(0.4 * handScalar, -0.1, 0.7);
        float localRoll = 0.0F;
        float localPitch = 0.0F;

        if (animationModule.getVisualCastState() == EntityAnimationModule.VisualCastState.CASTING) {
            float progress = animationModule.getCastRatio();
            float armZ = Mth.sin((float) (Math.PI / 2.0F + progress * Math.PI / 2.0F)) * (float) Math.PI / 4.0F;
            float angle = (float) (Math.PI / 2.0F + Mth.sin(progress * (float) Math.PI) * ((float) Math.PI / 8.0F));
            localRoll = (handSide == HumanoidArm.RIGHT ? -armZ : armZ) * 0.75F;
            localPitch = -angle * 0.35F;
            localOffset = localOffset.add(0.0, 0.08, 0.16);
        } else if (animationModule.getVisualCastState() == EntityAnimationModule.VisualCastState.RELEASE) {
            float progress = animationModule.getReleaseRatio();
            float armZ = Mth.cos((float) (Math.PI / 2.0F + progress * Math.PI)) * (float) Math.PI / 2.0F;
            localRoll = handSide == HumanoidArm.RIGHT ? -armZ : armZ;
        }

        localOffset = localOffset.zRot(localRoll).xRot(localPitch);
        return eyePos
                .add(right.scale(localOffset.x))
                .add(up.scale(localOffset.y))
                .add(look.scale(localOffset.z));
    }


    private Optional<Vec3> getHandPosition(float partialTicks, AbstractClientPlayer entityIn, HumanoidArm handSide) {
        return MCBone.getPositionOfBoneInWorld(entityIn, skeleton, partialTicks,
                getRenderOffset(entityIn, partialTicks), handSide == HumanoidArm.LEFT ?
                        BipedSkeleton.LEFT_HAND_BONE_NAME : BipedSkeleton.RIGHT_HAND_BONE_NAME);
    }
}
