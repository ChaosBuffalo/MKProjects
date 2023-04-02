package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.UIConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;


import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.UUID;

public class MKWidget extends MKAbstractGui implements IMKWidget {
    private final UUID id;
    private final LinkedList<IMKWidget> children;
    private IMKWidget parent;
    private IMKScreen screen;
    private int width;
    private int height;
    private int x;
    private int y;
    private int longHoverTicks;
    private boolean skipBoundsCheck;
    private boolean hovered;
    private float hoveredTicks;
    private boolean enabled;
    private boolean visible;
    private int debugColor;
    private boolean drawDebug;
    private boolean canFocus;
    private Component tooltip;

    public MKWidget(int x, int y, int width, int height){
        id = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        children = new LinkedList<>();
        parent = null;
        hovered = false;
        hoveredTicks = 0;
        longHoverTicks = UIConstants.DEFAULT_LONG_HOVER_TICKS;
        enabled = true;
        skipBoundsCheck = false;
        visible = true;
        screen = null;
        tooltip = null;
        debugColor = 0x3fffffff;
        canFocus = false;
    }

    @Override
    public boolean canFocus() {
        return canFocus;
    }

    public void setCanFocus(boolean canFocus) {
        this.canFocus = canFocus;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public LinkedList<IMKWidget> getChildren() {
        return children;
    }

    @Override
    public IMKWidget setParent(IMKWidget parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public void setDrawDebug(boolean value) {
        drawDebug = value;
    }

    @Override
    public void longHoverDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        IMKScreen screen = getScreen();
        if (tooltip != null && screen != null){
            // tooltips are added in screen space so we need to climb the widget tree to the top.
            Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
            screen.addPostRenderInstruction(new HoveringTextInstruction(tooltip, parentPos));
        }
    }

    @Override
    public IMKWidget setScreen(IMKScreen screen) {
        this.screen = screen;
        return this;
    }

    @Nullable
    @Override
    public IMKWidget getParent() {
        return parent;
    }

    @Nullable
    @Override
    public IMKScreen getScreen() {
        return screen;
    }

    @Override
    public IMKWidget setHeight(int newHeight) {
        this.height = newHeight;
        return this;
    }

    @Override
    public void setDebugColor(int color) {
        debugColor = color;
    }

    @Override
    public int getDebugColor() {
        return debugColor;
    }

    @Override
    public void drawDebugBounds(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        mkFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getDebugColor());
    }

    @Override
    public boolean doDrawDebugBounds() {
        return drawDebug;
    }

    @Override
    public IMKWidget setWidth(int newWidth) {
        this.width = newWidth;
        return this;
    }

    @Override
    public IMKWidget setX(int newX) {
        this.x = newX;
        return this;
    }

    @Override
    public IMKWidget setY(int newY) {
        this.y = newY;
        return this;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getLongHoverTicks() {
        return longHoverTicks;
    }

    @Override
    public IMKWidget setLongHoverTicks(int ticks) {
        this.longHoverTicks = ticks;
        return this;
    }

    @Override
    public boolean skipBoundsCheck() {
        return skipBoundsCheck;
    }

    @Override
    public IMKWidget setSkipBoundsCheck(boolean skipBoundsCheck) {
        this.skipBoundsCheck = skipBoundsCheck;
        return this;
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }

    @Override
    public IMKWidget setHovered(boolean value) {
        this.hovered = value;
        return this;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public IMKWidget setVisible(boolean value) {
        this.visible = value;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public IMKWidget setEnabled(boolean value) {
        this.enabled = value;
        return this;
    }

    @Override
    public float getHoveredTicks() {
        return hoveredTicks;
    }

    @Override
    public void setHoveredTicks(float value) {
        hoveredTicks = value;
    }

    @Override
    public IMKWidget setTooltip(Component newTooltip) {
        tooltip = newTooltip;
        return this;
    }

    @Override
    public void clearTooltip() {
        tooltip = null;
    }


}
