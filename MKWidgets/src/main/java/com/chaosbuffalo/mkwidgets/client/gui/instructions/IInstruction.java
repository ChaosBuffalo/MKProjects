package com.chaosbuffalo.mkwidgets.client.gui.instructions;

import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface IInstruction {

    void draw(GuiGraphics graphics, Font renderer, int screenWidth, int screenHeight, float partialTicks, MKScreen screen);
}
