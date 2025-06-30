package com.chaosbuffalo.mknpc.mixins.client;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixins {
    @Unique
    private LivingEntity toRender;

    @ModifyVariable(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private LivingEntity captureSource(LivingEntity entity) {
        this.toRender = entity;
        return entity;
    }

    @Redirect(
            method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType mknpc$proxyArmorCutoutNoCull(ResourceLocation loc) {
        if (toRender instanceof MKEntity mkEntity) {
            if (mkEntity.hasGhostArmor()) {
                return RenderType.entityTranslucent(loc, false);
            }
        }
        return RenderType.armorCutoutNoCull(loc);
    }

    @Redirect(
            method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            )
    )
    private void mknpc$proxyRenderToBuffer(Model model, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        if (toRender instanceof MKEntity mkEntity && mkEntity.hasGhostArmor()) {
            int alpha = FastColor.as8BitChannel(mkEntity.getGhostArmorTranslucency());
            int newColor = FastColor.ARGB32.color(alpha, packedColor);
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, newColor);
            return;
        }
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    @Redirect(
            method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
            )
    )
    private void mknpc$proxyRenderTrim(Model model, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        if (toRender instanceof MKEntity mkEntity && mkEntity.hasGhostArmor()) {
            int packedColor = FastColor.ARGB32.color(FastColor.as8BitChannel(mkEntity.getGhostArmorTranslucency()), 0xFFFFFF);
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
            return;
        }
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay);
    }
}
