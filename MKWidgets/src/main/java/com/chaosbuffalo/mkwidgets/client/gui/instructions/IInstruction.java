package com.chaosbuffalo.mkwidgets.client.gui.instructions;

import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;

public interface IInstruction {

    void draw(PoseStack matrixStack, Font renderer, int screenWidth, int screenHeight, float partialTicks, MKScreen screen);
}
