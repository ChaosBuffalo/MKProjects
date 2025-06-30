package com.chaosbuffalo.mknpc.mixins.client;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntityRenderer.class)
public class LivingRendererMixins {

    // 0x26FFFFFF == 0.15 alpha (38/255)
    @ModifyConstant(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            constant = @Constant(intValue = 0x26FFFFFF))
    private int modifyTransparency(int value, @Local(argsOnly = true) LivingEntity entity) {
        if (entity instanceof MKEntity mkEntity) {
            int alpha = FastColor.as8BitChannel(mkEntity.getTranslucency());
            return FastColor.ARGB32.color(alpha, 0xFFFFFF);
        }
        return value;
    }
}
