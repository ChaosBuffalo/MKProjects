package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.player.PlayerCombatExtensionModule;
import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Gui.class)
public abstract class InGameGuiMixins {
    @Unique
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    @Unique
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_progress");
    @Unique
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_full");
    @Unique
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background");
    @Unique
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress");

    @Shadow
    @Final
    protected Minecraft minecraft;

    /**
     * @author kovak
     * @reason testing better cursor rendering
     */
    @Inject(
            method = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
                    value = "INVOKE",
                    ordinal = 0
//                    args = {
//                            "log=true"
//                    }
            )
    )
    private void mkcore$colorCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Vector3f color = mkcore$getColorForSituation();
        RenderSystem.setShaderColor(color.x, color.y, color.z, 1.0f);
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V",
                    value = "INVOKE",
                    ordinal = 0
            )
    )
    private void mkcore$colorReset(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/Gui;renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL")
    )
    private void mkcore$renderOffhandAttackIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.options.attackIndicator().get() != AttackIndicatorStatus.HOTBAR) {
            return;
        }
        if (!(minecraft.player instanceof LocalPlayer localPlayer)) {
            return;
        }

        PlayerCombatExtensionModule combat = MKCore.getPlayerOrThrow(localPlayer).getCombatExtension();
        if (!combat.isDualWieldingMeleeWeapons()) {
            return;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        float strength = combat.getAttackStrengthScale(InteractionHand.OFF_HAND, partialTick);
        int centerX = guiGraphics.guiWidth() / 2;
        int y = guiGraphics.guiHeight() - 20;
        HumanoidArm offhandSide = localPlayer.getMainArm().getOpposite();
        int x = centerX + 91 + 6;
        if (offhandSide == HumanoidArm.RIGHT) {
            x = centerX - 91 - 22;
        }

        if (strength < 1.0F) {
            RenderSystem.enableBlend();
            int fill = Math.max(0, Math.min(18, (int) (strength * 19.0F)));
            blitMirroredSprite(guiGraphics, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
            if (fill > 0) {
                blitMirroredSprite(guiGraphics, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE,
                        18, 18, 0, 18 - fill, x, y + 18 - fill, 18, fill);
            }
            RenderSystem.disableBlend();
        }
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void mkcore$renderOffhandCrosshairAttackIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.options.attackIndicator().get() != AttackIndicatorStatus.CROSSHAIR) {
            return;
        }
        if (!(minecraft.player instanceof LocalPlayer localPlayer)) {
            return;
        }

        PlayerCombatExtensionModule combat = MKCore.getPlayerOrThrow(localPlayer).getCombatExtension();
        if (!combat.isDualWieldingMeleeWeapons()) {
            return;
        }

        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        float strength = combat.getAttackStrengthScale(InteractionHand.OFF_HAND, partialTick);
        boolean showFull = false;
        if (minecraft.crosshairPickEntity instanceof net.minecraft.world.entity.LivingEntity livingEntity && strength >= 1.0F) {
            showFull = combat.getRequiredAttackStrengthTicksForHand(InteractionHand.OFF_HAND) > 5.0F;
            showFull &= livingEntity.isAlive();
        }
        int x = guiGraphics.guiWidth() / 2 - 8;
        int y = guiGraphics.guiHeight() / 2 - 7 + 22;
        if (showFull) {
            blitMirroredSprite(guiGraphics, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, x, y, 16, 16);
        } else if (strength < 1.0F) {
            int fill = Math.max(0, Math.min(17, (int) (strength * 17.0F)));
            blitMirroredSprite(guiGraphics, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 16, 4);
            blitMirroredSprite(guiGraphics, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE,
                    16, 4, 0, 0, x + 16 - fill, y, fill, 4);
        }
    }

    @Unique
    private static final Vector3f COLOR_HOSTILE = new Vector3f(1.0f, 0.0f, 0.0f);
    @Unique
    private static final Vector3f COLOR_NEUTRAL = new Vector3f(1.0f, 1.0f, 0.0f);
    @Unique
    private static final Vector3f COLOR_FRIENDLY = new Vector3f(0.0f, 1.0f, 0.0f);
    @Unique
    private static final Vector3f COLOR_UNKNOWN = new Vector3f(1.0f, 1.0f, 1.0f);

    @Unique
    private Vector3f mkcore$getColorForSituation() {
        if (minecraft.player != null && minecraft.crosshairPickEntity != null) {
            Targeting.TargetRelation relation = Targeting.getTargetRelation(minecraft.player, minecraft.crosshairPickEntity);
            return switch (relation) {
                case FRIEND -> COLOR_FRIENDLY;
                case ENEMY -> COLOR_HOSTILE;
                case NEUTRAL -> COLOR_NEUTRAL;
                default -> COLOR_UNKNOWN;
            };
        }
        return COLOR_UNKNOWN;
    }

    @Unique
    private static void blitMirroredSprite(GuiGraphics guiGraphics, ResourceLocation sprite, int x, int y, int width, int height) {
        TextureAtlasSprite textureAtlasSprite = Minecraft.getInstance().getGuiSprites().getSprite(sprite);
        blitMirroredSprite(guiGraphics, textureAtlasSprite, x, y, 0, width, height);
    }

    @Unique
    private static void blitMirroredSprite(GuiGraphics guiGraphics, ResourceLocation sprite,
                                           int textureWidth, int textureHeight, int textureU, int textureV,
                                           int x, int y, int width, int height) {
        TextureAtlasSprite textureAtlasSprite = Minecraft.getInstance().getGuiSprites().getSprite(sprite);
        float minU = textureAtlasSprite.getU((float) (textureU + width) / (float) textureWidth);
        float maxU = textureAtlasSprite.getU((float) textureU / (float) textureWidth);
        float minV = textureAtlasSprite.getV((float) textureV / (float) textureHeight);
        float maxV = textureAtlasSprite.getV((float) (textureV + height) / (float) textureHeight);
        blitMirroredSprite(guiGraphics, textureAtlasSprite.atlasLocation(), minU, maxU, minV, maxV, x, y, 0, width, height);
    }

    @Unique
    private static void blitMirroredSprite(GuiGraphics guiGraphics, TextureAtlasSprite sprite, int x, int y, int z, int width, int height) {
        blitMirroredSprite(guiGraphics, sprite.atlasLocation(), sprite.getU1(), sprite.getU0(), sprite.getV0(), sprite.getV1(), x, y, z, width, height);
    }

    @Unique
    private static void blitMirroredSprite(GuiGraphics guiGraphics, ResourceLocation atlasLocation,
                                           float minU, float maxU, float minV, float maxV,
                                           int x, int y, int z, int width, int height) {
        RenderSystem.setShaderTexture(0, atlasLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        var matrix = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, (float) x, (float) y, (float) z).setUv(minU, minV);
        bufferBuilder.addVertex(matrix, (float) x, (float) (y + height), (float) z).setUv(minU, maxV);
        bufferBuilder.addVertex(matrix, (float) (x + width), (float) (y + height), (float) z).setUv(maxU, maxV);
        bufferBuilder.addVertex(matrix, (float) (x + width), (float) y, (float) z).setUv(maxU, minV);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}
