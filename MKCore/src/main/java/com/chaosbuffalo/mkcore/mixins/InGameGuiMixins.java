package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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
    protected void renderCrosshair(PoseStack matrixStack) {
        Options options = this.minecraft.options;
        if (options.getCameraType().isFirstPerson()) {
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
                if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo) {
                    Camera camera = this.minecraft.gameRenderer.getMainCamera();
                    PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.pushPose();

                    posestack.translate((double) (this.screenWidth / 2), (double) (this.screenHeight / 2), (double) this.getBlitOffset());
                    posestack.mulPose(Vector3f.XN.rotationDegrees(camera.getXRot()));
                    posestack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot()));
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
                    this.blit(matrixStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
                    if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                        float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                        boolean shouldDrawAttackIndicator = false;
                        Optional<Entity> pointedEntity = MKCore.getPlayer(minecraft.player).map(x -> x.getCombatExtension().getPointedEntity()).orElse(Optional.empty());
                        if (pointedEntity.isPresent() && pointedEntity.get() instanceof LivingEntity && f >= 1.0F) {
                            shouldDrawAttackIndicator = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                            shouldDrawAttackIndicator = shouldDrawAttackIndicator & pointedEntity.get().isAlive();
                        }

                        int j = this.screenHeight / 2 - 7 + 16;
                        int k = this.screenWidth / 2 - 8;
                        if (shouldDrawAttackIndicator) {
                            this.blit(matrixStack, k, j, 68, 94, 16, 16);
                        } else if (f < 1.0F) {
                            int l = (int) (f * 17.0F);
                            this.blit(matrixStack, k, j, 36, 94, 16, 4);
                            this.blit(matrixStack, k, j, 52, 94, l, 4);
                        }
                    }
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                }

            }
        }
    }

    private Vector3f getColorForSituation() {
        if (minecraft.player != null) {
            return MKCore.getPlayer(minecraft.player).map(x -> {
                Optional<Entity> target = x.getCombatExtension().getPointedEntity();
                return target.map(ent -> {
                    Targeting.TargetRelation relation = Targeting.getTargetRelation(minecraft.player, ent);
                    return switch (relation) {
                        case FRIEND -> new Vector3f(0.0f, 1.0f, 0.0f);
                        case ENEMY -> new Vector3f(1.0f, 0.0f, 0.0f);
                        case NEUTRAL -> new Vector3f(1.0f, 1.0f, 0.0f);
                        default -> new Vector3f(1.0f, 1.0f, 1.0f);
                    };
                }).orElse(new Vector3f(1.0f, 1.0f, 1.0f));
            }).orElse(new Vector3f(1.0f, 1.0f, 1.0f));
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }


}
