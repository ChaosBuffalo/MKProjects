package com.chaosbuffalo.mkwidgets.client.gui.instructions;

import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class DrawWidgetInstruction implements IInstruction {

    private final IMKWidget widget;
    private final Vec2i mousePos;
    private final Minecraft minecraft;

    public DrawWidgetInstruction(IMKWidget widget, Vec2i mousePos, Minecraft minecraft){
        this.widget = widget;
        this.mousePos = mousePos;
        this.minecraft = minecraft;
    }

    @Override
    public void draw(PoseStack matrixStack, Font renderer, int screenWidth, int screenHeight, float partialTicks, MKScreen screen) {
        widget.setX(mousePos.x - widget.getWidth() / 2);
        widget.setY(mousePos.y - widget.getHeight() / 2);
        widget.drawWidget(matrixStack, minecraft, mousePos.x, mousePos.y, partialTicks);
    }
}
