package com.chaosbuffalo.mkcore.mixins.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
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

    @Shadow
    private ClientLevel level;

    @Shadow
    public abstract void renderClouds(PoseStack matrixStackIn, Matrix4f mat, float partialTicks, double viewEntityX, double viewEntityY, double viewEntityZ);


    @Redirect(
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
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
            method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
            at = @At(
                    target = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;",
                    value = "INVOKE",
                    ordinal = 1,
                    shift = At.Shift.BY,
                    by = 2
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void mkcore$proxyRedoClouds(PoseStack p_109600_, float p_109601_, long p_109602_, boolean p_109603,
                                        Camera p_109604_, GameRenderer p_109605_, LightTexture p_109606_, Matrix4f p_109607_, CallbackInfo ci) {

        //draw clouds early before particles
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCloudsType() == CloudStatus.OFF) {
            return;
        }
        ProfilerFiller profilerfiller = level.getProfiler();
        Vec3 vector3d = p_109604_.getPosition();
        double d0 = vector3d.x();
        double d1 = vector3d.y();
        double d2 = vector3d.z();
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(p_109600_.last().pose());
        RenderSystem.applyModelViewMatrix();
        if (this.transparencyChain != null) {
            this.cloudsTarget.clear(Minecraft.ON_OSX);
            RenderStateShard.CLOUDS_TARGET.setupRenderState();
            profilerfiller.popPush("clouds");
            this.renderClouds(p_109600_, p_109607_, p_109601_, d0, d1, d2);
            RenderStateShard.CLOUDS_TARGET.clearRenderState();
        } else {
            profilerfiller.popPush("clouds");
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            this.renderClouds(p_109600_, p_109607_, p_109601_, d0, d1, d2);
        }
        posestack.popPose();
        //pretend clouds are off for rest of render loop, disables vanilla cloud rendering since we just did it
//        mc.options.cloudStatus().set(CloudStatus.OFF);;

    }

    // restore cloud options to original settings for next render
//    @Inject(method = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
//            at = @At("RETURN"),
//            locals = LocalCapture.CAPTURE_FAILHARD)
//    private void proxyEnd(PoseStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline,
//                          Camera activeRenderInfoIn, GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn, CallbackInfo ci) {
//        Minecraft mc = Minecraft.getInstance();
//        //set clouds back to previous setting
//        mc.options.cloudStatus().set(savedOption);
//    }
}
