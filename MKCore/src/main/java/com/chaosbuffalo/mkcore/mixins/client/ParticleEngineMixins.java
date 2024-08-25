package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.fx.particles.MKParticleRenderType;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixins {

    @Shadow
    @Final
    private TextureManager textureManager;

    @Inject(method= "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
    at= @At(
            target="Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            value ="INVOKE",
            shift = At.Shift.AFTER
    ))
    private void mkcore$render(LightTexture lightTexture, Camera camera, float partialTick,
                               Frustum frustum, Predicate<ParticleRenderType> renderTypePredicate,
                               CallbackInfo ci, @Local ParticleRenderType particlerendertype) {

        if (particlerendertype instanceof MKParticleRenderType mkType) {
            mkType.end(textureManager);
        }
    }
}
