package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.HeldItemParticleEffectRenderer;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.HeldItemParticleEffectInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixins {
    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void mkcore$renderHeldItemParticleAttachments(LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext,
                                                          HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer,
                                                          int packedLight, CallbackInfo callbackInfo) {
        InteractionHand hand = arm == entity.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        boolean leftHand = arm == HumanoidArm.LEFT;

        MKCore.getEntityData(entity).flatMap(data -> data.getParticleEffectTracker()).ifPresent(tracker -> {
            tracker.getParticleInstances().forEach(instance -> {
                if (instance instanceof HeldItemParticleEffectInstance heldItemInstance) {
                    HeldItemParticleEffectRenderer.spawnForRenderPose(heldItemInstance, entity, hand, poseStack, stack,
                            displayContext, leftHand, true, false);
                }
            });
        });
    }
}
