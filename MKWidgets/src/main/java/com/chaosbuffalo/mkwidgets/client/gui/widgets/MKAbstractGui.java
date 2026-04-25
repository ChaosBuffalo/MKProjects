package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Legacy extension point around {@link GuiGraphics}.
 * <p>
 * This type currently only preserves a named MKWidgets-specific abstraction for older helper code.
 */
public abstract class MKAbstractGui extends GuiGraphics {
    /**
     * @param pMinecraft active client instance
     * @param pBufferSource buffer source used for GUI rendering
     */
    public MKAbstractGui(Minecraft pMinecraft, MultiBufferSource.BufferSource pBufferSource) {
        super(pMinecraft, pBufferSource);
    }

//    public static void mkBlitUVSizeSame(PoseStack matrixStack, int x, int y, float u0, float v0, int width, int height,
//                                        int texWidth, int texHeight) {
//        blit(matrixStack, x, y, u0, v0, width, height, texWidth, texHeight);
//    }
//
//    public static void mkBlitUVSizeDifferent(PoseStack matrixStack, int x, int y, int width, int height, float u0, float v0,
//                                             int uSize, int vSize, int texWidth, int texHeight) {
//        blit(matrixStack, x, y, width, height, u0, v0, uSize, vSize, texWidth, texHeight);
//    }
//
//    public static void mkFill(PoseStack matrixStack, int x0, int y0, int x1, int y1, int color) {
//        fill(matrixStack, x0, y0, x1, y1, color);
//    }
}
