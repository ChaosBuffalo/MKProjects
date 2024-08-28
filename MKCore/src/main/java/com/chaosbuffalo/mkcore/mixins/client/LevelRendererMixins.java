package com.chaosbuffalo.mkcore.mixins.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixins {
    @Shadow
    @Nullable
    private PostChain transparencyChain;

    @Shadow
    @Nullable
    private RenderTarget cloudsTarget;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    public abstract void renderClouds(PoseStack poseStack, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ);


    @Redirect(
            method = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            at = @At(
                    target = "Lnet/minecraft/client/Options;getCloudsType()Lnet/minecraft/client/CloudStatus;",
                    value = "INVOKE"
            )
    )
    private CloudStatus mkcore$interceptCloudStatus(Options instance) {
        return CloudStatus.OFF;
    }

    // move clouds to before particle rendering
    @Inject(
            method = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            at = @At(
                    target = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;",
                    value = "INVOKE",
                    ordinal = 1,
                    shift = At.Shift.BY,
                    by = 2
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void mkcore$proxyRedoClouds(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
                                        GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix,
                                        Matrix4f projectionMatrix, CallbackInfo ci, @Local PoseStack posestack,
                                        @Local ProfilerFiller profilerfiller, @Local(ordinal = 0) float f, @Local(ordinal = 0) double d0, @Local(ordinal = 1) double d1, @Local(ordinal = 2) double d2) {

        //draw clouds early before particles
        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF) {
            if (this.transparencyChain != null) {
                this.cloudsTarget.clear(Minecraft.ON_OSX);
            }

            profilerfiller.popPush("clouds");
            this.renderClouds(posestack, frustumMatrix, projectionMatrix, f, d0, d1, d2);
        }
    }
}
