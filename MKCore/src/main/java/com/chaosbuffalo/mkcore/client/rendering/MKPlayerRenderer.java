package com.chaosbuffalo.mkcore.client.rendering;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.rendering.model.MKPlayerModel;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.core.player.PlayerAnimationModule;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
            PlayerAnimationModule.PlayerVisualCastState state = data.getAnimationModule().getPlayerVisualCastState();
            if (state == PlayerAnimationModule.PlayerVisualCastState.CASTING || state == PlayerAnimationModule.PlayerVisualCastState.RELEASE) {
                MKAbility ability = data.getAnimationModule().getCastingAbility();
                if (ability != null) {
                    // do spell casting
                    if (ability.hasCastingParticles()) {
                        ParticleAnimation anim = ParticleAnimationManager.getAnimation(ability.getCastingParticles());
                        if (anim != null) {
                            Optional<Vec3> leftPos = getHandPosition(partialTicks, entityIn, HumanoidArm.LEFT);
                            leftPos.ifPresent(x -> anim.spawn(entityIn.getCommandSenderWorld(), x, null));
                            Optional<Vec3> rightPos = getHandPosition(partialTicks, entityIn, HumanoidArm.RIGHT);
                            rightPos.ifPresent(x -> anim.spawn(entityIn.getCommandSenderWorld(), x, null));
                        }
                    }
                }
            }
            data.getAnimationModule().getParticleInstances().forEach(instance -> {
                instance.update(entityIn, skeleton, partialTicks, getRenderOffset(entityIn, partialTicks));
            });
        });
    }

    @Override
    public void renderRightHand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn) {
        super.renderRightHand(matrixStackIn, bufferIn, combinedLightIn, playerIn);
        if (playerIn instanceof LocalPlayer) {
            MKCore.getPlayer(playerIn).ifPresent(data -> {
                PlayerAnimationModule.PlayerVisualCastState state = data.getAnimationModule().getPlayerVisualCastState();
                if (state == PlayerAnimationModule.PlayerVisualCastState.CASTING || state == PlayerAnimationModule.PlayerVisualCastState.RELEASE) {
                    MKAbility ability = data.getAnimationModule().getCastingAbility();
                    if (ability != null) {
                        // do spell casting
                        if (ability.hasCastingParticles()) {
                            ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(ability.getCastingParticles());
                            if (anim != null) {
                                Vec3 leftPos = getFirstPersonHandPosition(HumanoidArm.LEFT,
                                        (LocalPlayer) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getCommandSenderWorld(), leftPos, null);
                                Vec3 rightPos = getFirstPersonHandPosition(HumanoidArm.RIGHT,
                                        (LocalPlayer) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getCommandSenderWorld(), rightPos, null);
                            }
                        }
                    }
                }


                data.getAnimationModule().getParticleInstances().forEach(instance -> {
                    instance.update(playerIn, skeleton, 0.0f, getRenderOffset(playerIn, 0.0f));
                });
            });
        }

    }

    @Override
    public void renderLeftHand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn) {
        super.renderLeftHand(matrixStackIn, bufferIn, combinedLightIn, playerIn);
        if (playerIn instanceof LocalPlayer) {
            MKCore.getPlayer(playerIn).ifPresent(data -> {
                PlayerAnimationModule.PlayerVisualCastState state = data.getAnimationModule().getPlayerVisualCastState();
                if (state == PlayerAnimationModule.PlayerVisualCastState.CASTING || state == PlayerAnimationModule.PlayerVisualCastState.RELEASE) {
                    MKAbility ability = data.getAnimationModule().getCastingAbility();
                    if (ability != null) {
                        // do spell casting
                        if (ability.hasCastingParticles()) {
                            ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(ability.getCastingParticles());
                            if (anim != null) {
                                Vec3 leftPos = getFirstPersonHandPosition(HumanoidArm.LEFT,
                                        (LocalPlayer) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getCommandSenderWorld(), leftPos, null);
                                Vec3 rightPos = getFirstPersonHandPosition(HumanoidArm.RIGHT,
                                        (LocalPlayer) playerIn, 0.0f, getRenderOffset(playerIn, 0.0f));
                                anim.spawn(playerIn.getCommandSenderWorld(), rightPos, null);
                            }
                        }
                    }
                }
                data.getAnimationModule().getParticleInstances().forEach(instance -> {
                    instance.update(playerIn, skeleton, 0.0f, getRenderOffset(playerIn, 0.0f));
                });
            });
        }
    }

    private Vec3 getOffsetSideFirstPerson(HumanoidArm handIn, float equippedProg) {
        int i = handIn == HumanoidArm.RIGHT ? 1 : -1;
        return new Vec3(i * 0.56F, -0.52F + equippedProg * -0.6F, -0.72F);
    }

    private Vec3 getFirstPersonHandPosition(HumanoidArm handSide,
                                            LocalPlayer playerEntityIn, float partialTicks,
                                            Vec3 renderOffset) {
        double entX = Mth.lerp(partialTicks, playerEntityIn.xo, playerEntityIn.getX());
        double entY = Mth.lerp(partialTicks, playerEntityIn.yo, playerEntityIn.getY());
        double entZ = Mth.lerp(partialTicks, playerEntityIn.zo, playerEntityIn.getZ());
        float yaw = Mth.lerp(partialTicks, playerEntityIn.yBodyRotO, playerEntityIn.yBodyRot) * ((float) Math.PI / 180F);
        int handScalar = handSide == HumanoidArm.RIGHT ? 1 : -1;
        // taken from first person render pathway
        Vec3 shoulderLoc = new Vec3(handScalar * -0.4785682F, -0.094387F, 0.05731531F);
        // the rest from bone system
        MCBone shoulderBone = handSide == HumanoidArm.RIGHT ? skeleton.rightArm : skeleton.leftArm;
        MCBone castLoc = handSide == HumanoidArm.RIGHT ? skeleton.rightHand : skeleton.leftHand;
        Vec3 bonePos = MCBone.getOffsetForStopAt(castLoc, shoulderBone);
        bonePos = bonePos.add(shoulderLoc);
        //a height fudge factor
        return new Vec3(entX, entY, entZ).add(renderOffset).add(new Vec3(0.0, 1.25, 0.0)).add(bonePos.yRot(-yaw));
    }


    private Optional<Vec3> getHandPosition(float partialTicks, AbstractClientPlayer entityIn, HumanoidArm handSide) {
        return MCBone.getPositionOfBoneInWorld(entityIn, skeleton, partialTicks,
                getRenderOffset(entityIn, partialTicks), handSide == HumanoidArm.LEFT ?
                        BipedSkeleton.LEFT_HAND_BONE_NAME : BipedSkeleton.RIGHT_HAND_BONE_NAME);
    }
}
