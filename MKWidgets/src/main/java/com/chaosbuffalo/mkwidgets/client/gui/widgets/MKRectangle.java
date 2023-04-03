package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class MKRectangle extends MKWidget {
    private int color;

    public MKRectangle(int x, int y, int width, int height, int color) {
        super(x, y, width, height);
        this.color = color;
    }

    public static MKRectangle GetHorizontalBar(int width, int height, int color) {
        return new MKRectangle(0, 0, width, height, color);
    }

    public static MKRectangle GetHorizontalBar(int height, int color) {
        return GetHorizontalBar(200, height, color);
    }

    public static MKRectangle GetVerticalBar(int width, int height, int color) {
        return new MKRectangle(0, 0, width, height, color);
    }

    public static MKRectangle GetVerticalBar(int width, int color) {
        return GetVerticalBar(width, 200, color);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void draw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        mkFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getColor());
    }
}