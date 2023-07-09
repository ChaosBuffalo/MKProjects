package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;


@Mixin(Gui.class)
public abstract class InGameGuiMixins extends GuiComponent {

    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    protected abstract boolean canRenderCrosshairForSpectator(HitResult rayTraceIn);

    @Shadow
    protected int screenWidth;

    @Shadow
    protected int screenHeight;

    /**
     * @author kovak
     * @reason testing better cursor rendering
     */

    @Overwrite
    public void renderCrosshair(PoseStack matrixStack) {
        Options options = this.minecraft.options;
        if (options.getCameraType().isFirstPerson()) {
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
                if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
                    Camera camera = this.minecraft.gameRenderer.getMainCamera();
                    PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.pushPose();

                    posestack.translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
                    posestack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
                    posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
                    posestack.scale(-1.0F, -1.0F, -1.0F);
                    RenderSystem.applyModelViewMatrix();
                    RenderSystem.renderCrosshair(10);
                    posestack.popPose();
                    RenderSystem.applyModelViewMatrix();
                } else {
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    int i = 15;
                    Vector3f color = getColorForSituation();
                    RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
                    RenderSystem.setShaderColor(color.x(), color.y(), color.z(), 1.0f);
                    blit(matrixStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                        float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                        boolean shouldDrawAttackIndicator = false;
                        if (minecraft.crosshairPickEntity != null &&
                                minecraft.crosshairPickEntity instanceof LivingEntity livingEntity &&
                                f >= 1.0F) {
                            shouldDrawAttackIndicator = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                            shouldDrawAttackIndicator = shouldDrawAttackIndicator & livingEntity.isAlive();
                        }

                        int j = this.screenHeight / 2 - 7 + 16;
                        int k = this.screenWidth / 2 - 8;
                        if (shouldDrawAttackIndicator) {
                            blit(matrixStack, k, j, 68, 94, 16, 16);
                        } else if (f < 1.0F) {
                            int l = (int) (f * 17.0F);
                            blit(matrixStack, k, j, 36, 94, 16, 4);
                            blit(matrixStack, k, j, 52, 94, l, 4);
                        }
                    }
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                }

            }
        }
    }

    private Vector3f getColorForSituation() {
        if (minecraft.player != null && minecraft.crosshairPickEntity != null) {
            Targeting.TargetRelation relation = Targeting.getTargetRelation(minecraft.player, minecraft.crosshairPickEntity);
            return switch (relation) {
                case FRIEND -> new Vector3f(0.0f, 1.0f, 0.0f);
                case ENEMY -> new Vector3f(1.0f, 0.0f, 0.0f);
                case NEUTRAL -> new Vector3f(1.0f, 1.0f, 0.0f);
                default -> new Vector3f(1.0f, 1.0f, 1.0f);
            };
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }


}
