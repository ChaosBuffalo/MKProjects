package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.EntityAnimationModule;
import com.chaosbuffalo.mkcore.core.player.PlayerCombatExtensionModule;
import com.chaosbuffalo.mkcore.client.rendering.HeldItemParticleEffectRenderer;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.HeldItemParticleEffectInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixins {
    @Shadow
    private ItemRenderer itemRenderer;

    @Shadow
    private void renderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
                                   float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack,
                                   MultiBufferSource buffer, int combinedLight) {
    }

    @Shadow
    private void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equippedProgress) {
    }

    @Redirect(
            method = "renderHandsWithItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void mkcore$useHandSpecificSwingProgress(ItemInHandRenderer instance, AbstractClientPlayer player,
                                                     float partialTicks, float pitch, InteractionHand hand,
                                                     float swingProgress, ItemStack stack, float equippedProgress,
                                                     PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
        float resolvedSwing = swingProgress;
        float resolvedEquippedProgress = equippedProgress;
        if (player instanceof LocalPlayer localPlayer) {
            PlayerCombatExtensionModule combat = MKCore.getPlayerOrThrow(localPlayer).getCombatExtension();
            float visualSwing = combat.getVisualMeleeAttackAnim(hand, partialTicks);
            if (combat.hasActiveVisualMeleeAttack(hand, partialTicks)) {
                resolvedSwing = visualSwing;
                resolvedEquippedProgress = 0.0F;
            }
        }
        renderArmWithItem(player, partialTicks, pitch, hand, resolvedSwing, stack, resolvedEquippedProgress, poseStack, buffer, combinedLight);
    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"
            )
    )
    private void mkcore$applySpellPoseAfterHandPlacement(ItemInHandRenderer instance, PoseStack poseStack, HumanoidArm arm,
                                                         float equippedProgress, AbstractClientPlayer player, float partialTicks,
                                                         float pitch, InteractionHand hand, float swingProgress, ItemStack stack,
                                                         float passedEquippedProgress, PoseStack passedPoseStack,
                                                         MultiBufferSource buffer, int combinedLight) {
        applyItemArmTransform(poseStack, arm, equippedProgress);
        if (player instanceof LocalPlayer localPlayer) {
            applyFirstPersonSpellPose(localPlayer, hand, arm, poseStack);
        }
    }

    private void applyFirstPersonSpellPose(LocalPlayer player, InteractionHand hand, HumanoidArm arm, PoseStack poseStack) {
        var playerData = MKCore.getPlayer(player).orElse(null);
        if (playerData == null) {
            return;
        }
        PlayerCombatExtensionModule combat = playerData.getCombatExtension();
        if (combat.hasActiveVisualMeleeAttack(hand, 0.0F)) {
            return;
        }
        EntityAnimationModule animationModule = playerData.getAnimationModule();
        if (animationModule.getCastingAbility() == null) {
            return;
        }
        float progress;
        if (animationModule.getVisualCastState() == EntityAnimationModule.VisualCastState.CASTING) {
            progress = animationModule.getCastRatio();
        } else if (animationModule.getVisualCastState() == EntityAnimationModule.VisualCastState.RELEASE) {
            progress = animationModule.getReleaseRatio();
        } else {
            return;
        }

        if (progress <= 0.0F) {
            return;
        }
        if (animationModule.getVisualCastState() == EntityAnimationModule.VisualCastState.CASTING) {
            float armZ = Mth.sin((float) (Math.PI / 2.0F + progress * Math.PI / 2.0F)) * (float) Math.PI / 4.0F;
            float angle = (float) (Math.PI / 2.0F + Mth.sin(progress * (float) Math.PI) * ((float) Math.PI / 8.0F));
            float zDegrees = (arm == HumanoidArm.RIGHT ? -armZ : armZ) * Mth.RAD_TO_DEG;
            poseStack.mulPose(Axis.ZP.rotationDegrees(zDegrees));
            poseStack.mulPose(Axis.XP.rotationDegrees(-angle * Mth.RAD_TO_DEG));
        } else {
            float armZ = Mth.cos((float) (Math.PI / 2.0F + progress * Math.PI)) * (float) Math.PI / 2.0F;
            float zDegrees = (arm == HumanoidArm.RIGHT ? -armZ : armZ) * Mth.RAD_TO_DEG;
            poseStack.mulPose(Axis.ZP.rotationDegrees(zDegrees));
        }
    }



    @Inject(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V"
            )
    )
    private void mkcore$renderHeldItemParticleAttachments(LivingEntity entity, ItemStack itemStack,
                                                          ItemDisplayContext displayContext, boolean leftHand,
                                                          PoseStack poseStack, MultiBufferSource buffer, int seed, CallbackInfo ci) {
        if (!(entity instanceof LocalPlayer localPlayer)) {
            return;
        }

        MKCore.getPlayer(localPlayer).ifPresent(data -> {
            if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            {
                InteractionHand hand = (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND &&
                        localPlayer.getMainArm() == HumanoidArm.LEFT) || (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND &&
                        localPlayer.getMainArm() == HumanoidArm.RIGHT) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                data.getAnimationModule().getParticleInstances().forEach(instance -> {
                    if (instance instanceof HeldItemParticleEffectInstance heldItemInstance) {
                        HeldItemParticleEffectRenderer.spawnForRenderPose(heldItemInstance, localPlayer, hand, poseStack, itemStack,
                                displayContext,
                                leftHand, true, false);
                    }
                });
            }
        });
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
            )
    )
    private float mkcore$preserveMainhandEquipDuringVisualSwing(LocalPlayer player, float partialTicks) {
        PlayerCombatExtensionModule combat = MKCore.getPlayerOrThrow(player).getCombatExtension();
        if (combat.usesCustomMainhandMelee()) {
            return 1.0F;
        }
        return player.getAttackStrengthScale(partialTicks);
    }
}
