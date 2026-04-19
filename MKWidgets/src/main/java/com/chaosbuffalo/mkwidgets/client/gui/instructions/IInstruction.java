package com.chaosbuffalo.mkwidgets.client.gui.instructions;

import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Post-render instruction scheduled by widgets or screens for work that must happen after the main tree draws.
 */
public interface IInstruction {

    /**
     * Draws the queued instruction.
     *
     * @param graphics active GUI graphics context
     * @param renderer screen font renderer
     * @param screenWidth current screen width
     * @param screenHeight current screen height
     * @param partialTicks current partial tick value
     * @param screen screen executing the instruction
     */
    void draw(GuiGraphics graphics, Font renderer, int screenWidth, int screenHeight, float partialTicks, MKScreen screen);
}
