package com.chaosbuffalo.mknpc.mixins;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Map;

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
            method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IZLnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
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


    @ModifyConstant(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IZLnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            constant = @Constant(floatValue = 1.0f),
            remap = false)
    private float modifyTransparency(float value) {
        if (toRender instanceof MKEntity mkEntity && mkEntity.hasGhostArmor()) {
            return mkEntity.getGhostArmorTranslucency();
        }
        return value;
    }

    @ModifyConstant(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;ZLnet/minecraft/client/model/HumanoidModel;ZFFF)V",
            constant = @Constant(floatValue = 1.0f))
    private float modifyTrimTransparency(float value) {
        if (toRender instanceof MKEntity mkEntity && mkEntity.hasGhostArmor()) {
            return mkEntity.getGhostArmorTranslucency();
        }
        return value;
    }
}
