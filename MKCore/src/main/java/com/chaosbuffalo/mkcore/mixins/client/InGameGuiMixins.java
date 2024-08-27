package com.chaosbuffalo.mkcore.mixins.client;

import com.chaosbuffalo.targeting_api.Targeting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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
}
