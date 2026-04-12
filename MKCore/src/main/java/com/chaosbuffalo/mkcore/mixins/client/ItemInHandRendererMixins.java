package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.player.PlayerCombatExtensionModule;
import com.chaosbuffalo.mkcore.client.rendering.HeldItemParticleEffectRenderer;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.HeldItemParticleEffectInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
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
