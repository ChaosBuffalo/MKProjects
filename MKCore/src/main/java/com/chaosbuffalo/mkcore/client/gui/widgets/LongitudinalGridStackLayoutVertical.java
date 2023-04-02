package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;

public class LongitudinalGridStackLayoutVertical extends MKStackLayoutVertical {
    private final int gridWidth;
    private final int gridSpacing;
    private int gridCount;
    private final int gridColor;
    private final Font font;

    public LongitudinalGridStackLayoutVertical(int x, int y, int width, int gridWidth, int gridSpacing, int gridCount, int gridColor,
                                               Font fontRenderer) {
        super(x, y, width);
        this.gridWidth = gridWidth;
        this.gridSpacing = gridSpacing;
        this.gridCount = gridCount;
        this.gridColor = gridColor;
        this.font = fontRenderer;
        setMarginTop(10);
        setMarginLeft(4);
    }

    public int getDesiredWidth() {
        return gridCount * (gridSpacing + gridWidth);
    }

    public void setGridCount(int newCount) {
        this.gridCount = newCount;
    }

    public int getGridCount() {
        return gridCount;
    }

    public int getGridPos(int index) {
        return index * (gridSpacing + gridWidth);
    }

    @Override
    public void preDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        mkFill(matrixStack, getX() + 2, getY() + 10, getX() + 2 + gridCount * (gridSpacing + gridWidth), getY() + 9, gridColor);
        for (int i = 0; i <= gridCount; i++) {
            int xPos = i * (gridSpacing + gridWidth);
            int rowColor = i % 4 == 0 ? gridColor & 0xbbffffff : gridColor;
            mkFill(matrixStack, getX() + xPos + 2, getY() + 10, getX() + 2 + xPos + gridWidth,
                    getY() + getHeight() + 10, rowColor);
            if (i % 4 == 0) {
                String text = String.format("%d", i / 4);
                font.draw(matrixStack,
                        new TextComponent(text), getX() + 2 + xPos - (font.width(text) / 2),
                        getY(), gridColor);
            }
        }
    }
}
