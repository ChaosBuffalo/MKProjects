package com.chaosbuffalo.mkcore.mixins;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixins {
    private CloudStatus savedOption;

    @Shadow
    @Nullable
    private PostChain transparencyChain;

    @Shadow
    @Nullable
    private RenderTarget cloudsTarget;

    @Shadow
    private ClientLevel level;

    //public void renderClouds(PoseStack p_172955_, Matrix4f p_172956_, float p_172957_, double p_172958_, double p_172959_, double p_172960_)

    @Shadow
    public abstract void renderClouds(PoseStack matrixStackIn, Matrix4f mat, float partialTicks, double viewEntityX, double viewEntityY, double viewEntityZ);


    // move clouds to before particle rendering
    @Inject(method = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V",
            at = @At(target = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;", value = "INVOKE", ordinal = 2, shift = At.Shift.BY, by = 2),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void proxyRedoClouds(PoseStack p_109600_, float p_109601_, long p_109602_, boolean p_109603,
                                 Camera p_109604_, GameRenderer p_109605_, LightTexture p_109606_, Matrix4f p_109607_, CallbackInfo ci) {

        //draw clouds early before particles
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderClouds == CloudStatus.OFF) {
            savedOption = mc.options.getCloudsType();
            return;
        }
        ProfilerFiller profilerfiller = level.getProfiler();
        Vec3 vector3d = p_109604_.getPosition();
        double d0 = vector3d.x();
        double d1 = vector3d.y();
        double d2 = vector3d.z();
        p_109600_.pushPose();
        p_109600_.mulPoseMatrix(p_109600_.last().pose());
        RenderSystem.applyModelViewMatrix();
        if (mc.options.getCloudsType() != CloudStatus.OFF) {
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
        }
        p_109600_.popPose();
        //pretend clouds are off for rest of render loop, disables vanilla cloud rendering since we just did it
        mc.options.renderClouds = CloudStatus.OFF;

    }

    // restore cloud options to original settings for next render
    @Inject(method = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void proxyEnd(PoseStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline,
                          Camera activeRenderInfoIn, GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        //set clouds back to previous setting
        mc.options.renderClouds = savedOption;
    }
}
