package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;

public class MCWidgetContainer extends MKWidget {

    private final AbstractWidget mcWidget;


    public MCWidgetContainer(int x, int y, int width, int height, AbstractWidget mcWidget, boolean canFocus) {
        super(x, y, width, height);
        this.mcWidget = mcWidget;
        mcWidget.setX(x);
        mcWidget.setY(y);
        mcWidget.setWidth(width);
        mcWidget.setHeight(height);
        setCanFocus(canFocus);
    }

    public AbstractWidget getContainedWidget() {
        return mcWidget;
    }

    @Override
    public IMKWidget setHeight(int newHeight) {
        mcWidget.setHeight(newHeight);
        return super.setHeight(newHeight);
    }

    @Override
    public IMKWidget setWidth(int newWidth) {
        mcWidget.setWidth(newWidth);
        return super.setWidth(newWidth);
    }

    @Override
    public IMKWidget setX(int newX) {
        mcWidget.setX(newX);
        return super.setX(newX);
    }

    @Override
    public IMKWidget setY(int newY) {
        mcWidget.setY(newY);
        return super.setY(newY);
    }

    @Override
    public IMKWidget setVisible(boolean value) {
        mcWidget.visible = true;
        return super.setVisible(value);
    }

    @Override
    public void draw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        mcWidget.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        return mcWidget.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        return mcWidget.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton, double dX, double dY) {
        return mcWidget.mouseDragged(mouseX, mouseY, mouseButton, dX, dY);
    }

    @Override
    public boolean onMouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double distance) {
        return mcWidget.mouseScrolled(mouseX, mouseY, distance);
    }

    @Override
    public boolean keyPressed(Minecraft minecraft, int keyCode, int scanCode, int modifiers) {
        return mcWidget.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(Minecraft minecraft, int keyCode, int scanCode, int modifiers) {
        return mcWidget.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(Minecraft minecraft, char codePoint, int modifiers) {
        return mcWidget.charTyped(codePoint, modifiers);
    }
}
