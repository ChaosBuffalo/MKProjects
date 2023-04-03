package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public interface IMKWidget {

    default boolean addWidget(IMKWidget widget) {
        widget.setParent(this);
        widget.inheritScreen(getScreen());
        getChildren().add(widget);
        return true;
    }

    default void inheritScreen(IMKScreen screen) {
        setScreen(screen);
        for (IMKWidget widget : getChildren()) {
            widget.inheritScreen(screen);
        }
    }

    default void removeWidget(IMKWidget widget) {
        if (widget.getParent() != null && widget.getParent().getId().equals(this.getId())) {
            getChildren().removeIf((x) -> x.getId().equals(widget.getId()));
            widget.setParent(null);
            widget.inheritScreen(null);
        }
    }

    default void findFocusable(List<IMKWidget> tree) {
        if (canFocus()) {
            tree.add(this);
        }
        for (IMKWidget wid : getChildren()) {
            wid.findFocusable(tree);
        }
    }

    UUID getId();

    LinkedList<IMKWidget> getChildren();

    IMKWidget setParent(IMKWidget parent);

    IMKWidget setScreen(IMKScreen screen);

    @Nullable
    IMKWidget getParent();

    @Nullable
    IMKScreen getScreen();

    IMKWidget setHeight(int newHeight);

    IMKWidget setWidth(int newWidth);

    IMKWidget setX(int newX);

    IMKWidget setY(int newY);

    int getWidth();

    int getHeight();

    int getX();

    int getY();

    default int getRight() {
        return getX() + getWidth();
    }

    default int getTop() {
        return getY();
    }

    default int getLeft() {
        return getX();
    }

    default int getBottom() {
        return getY() + getHeight();
    }

    default Vec2i getParentCoords(Vec2i pos) {
        if (getParent() == null) {
            return pos;
        } else {
            return getParent().getParentCoords(pos);
        }
    }

    int getLongHoverTicks();

    default boolean canFocus() {
        return false;
    }

    default void onFocus() {
    }

    default void onFocusLost() {
    }

    IMKWidget setLongHoverTicks(int ticks);

    boolean skipBoundsCheck();

    IMKWidget setSkipBoundsCheck(boolean value);

    boolean isHovered();

    IMKWidget setHovered(boolean value);

    boolean isVisible();

    IMKWidget setVisible(boolean value);

    boolean isEnabled();

    IMKWidget setEnabled(boolean value);

    default boolean isInBounds(double x, double y) {
        if (skipBoundsCheck()) {
            return true;
        }
        return x >= getX() && y >= getY() && x < getRight() && y < getBottom();
    }

    default void preDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }

    default void draw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }

    default void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }

    default void longHoverDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height,
                               int mouseX, int mouseY, float partialTicks) {

    }

    float getHoveredTicks();

    void setHoveredTicks(float value);

    default boolean checkHovered(int mouseX, int mouseY) {
        return isVisible() && isEnabled() && isInBounds(mouseX, mouseY);
    }

    default void handleLongHoverDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height,
                                     int mouseX, int mouseY, float partialTicks) {
        if (isHovered() && getHoveredTicks() > getLongHoverTicks()) {
            longHoverDraw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        }
    }

    default IMKWidget setTooltip(String newTooltip) {
        return setTooltip(new TextComponent(newTooltip));
    }

    IMKWidget setTooltip(Component text);

    void clearTooltip();

    default void handleHoverDetection(int mouseX, int mouseY, float partialTicks) {
        boolean hovered = checkHovered(mouseX, mouseY);
        if (hovered) {
            setHoveredTicks(getHoveredTicks() + partialTicks);
        } else {
            setHoveredTicks(0);
        }
        setHovered(hovered);
    }


    default void clearHovered() {
        setHoveredTicks(0);
        setHovered(false);
        for (IMKWidget child : getChildren()) {
            child.clearHovered();
        }
    }

    boolean doDrawDebugBounds();

    default void drawDebugBounds(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX,
                                 int mouseY, float partialTicks) {

    }

    void setDrawDebug(boolean value);

    void setDebugColor(int color);

    int getDebugColor();

    default void onMouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        handleHoverDetection(mouseX, mouseY, partialTicks);
    }

    default void mouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!checkHovered(mouseX, mouseY)) {
            clearHovered();
            return;
        }
        for (IMKWidget child : getChildren()) {
            child.mouseHover(mc, mouseX, mouseY, partialTicks);
        }
        onMouseHover(mc, mouseX, mouseY, partialTicks);
    }

    default void drawChildren(PoseStack matrixStack, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (IMKWidget child : getChildren()) {
            if (child.isVisible()) {
                child.drawWidget(matrixStack, mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    default void drawWidget(PoseStack matrixStack, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        if (doDrawDebugBounds()) {
            drawDebugBounds(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        }
        preDraw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        draw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        drawChildren(matrixStack, mc, mouseX, mouseY, partialTicks);
        postDraw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        handleLongHoverDraw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
    }

    default void clearWidgets() {
        for (IMKWidget widget : getChildren()) {
            widget.setParent(null);
        }
        getChildren().clear();
    }

    @Nullable
    default IMKWidget getChild(int index) {
        return getChildren().get(index);
    }

    default boolean onMouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double distance) {
        return false;
    }

    default boolean mouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double distance) {
        if (!this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        if (!this.isEnabled()) {
            return true;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mouseScrollWheel(minecraft, mouseX, mouseY, distance)) {
                return true;
            }
        }
        return onMouseScrollWheel(minecraft, mouseX, mouseY, distance);
    }

    default void onDragEnd(IDragState state) {

    }

    default boolean mouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton,
                                 double dX, double dY) {
        if (!this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        if (!this.isEnabled()) {
            return true;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mouseDragged(minecraft, mouseX, mouseY, mouseButton, dX, dY)) {
                return true;
            }
        }
        return onMouseDragged(minecraft, mouseX, mouseY, mouseButton, dX, dY);
    }

    default boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton,
                                   double dX, double dY) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (!this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        if (!this.isEnabled()) {
            return true;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mouseReleased(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return onMouseRelease(mouseX, mouseY, mouseButton);
    }

    default boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    default boolean mousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (!this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        if (!this.isEnabled()) {
            return true;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mousePressed(minecraft, mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        boolean consumedBySelf = onMousePressed(minecraft, mouseX, mouseY, mouseButton);
        if (consumedBySelf) {
            IMKScreen screen = getScreen();
            if (screen != null) {
                if (canFocus()) {
                    screen.setFocus(this);
                } else {
                    screen.setFocus(null);
                }
            }
        }
        return consumedBySelf;
    }

    default boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    default boolean keyPressed(Minecraft minecraft, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean keyReleased(Minecraft minecraft, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(Minecraft minecraft, char codePoint, int modifiers) {
        return false;
    }
}
