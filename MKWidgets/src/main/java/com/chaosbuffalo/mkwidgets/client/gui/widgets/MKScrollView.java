package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.util.Iterator;

public class MKScrollView extends MKWidget {

    private double offsetX;
    private double offsetY;
    private int screenWidth;
    private int screenHeight;
    private boolean isDragging;
    private boolean clipBounds;
    private double scaleFactor;
    private boolean doScrollLock;
    private int scrollMarginX;
    private int scrollMarginY;
    private boolean doScrollX;
    private boolean doScrollY;
    private double scrollVelocity;
    private boolean drawScrollBars;
    private static final int SCROLL_BAR_WIDTH = 1;

    public MKScrollView(int x, int y, int width, int height, double scaleFactor,
                        boolean clipBounds) {
        super(x, y, width, height);
        offsetX = 0;
        offsetY = 0;
        this.clipBounds = clipBounds;
        isDragging = false;
        this.scaleFactor = scaleFactor;
        this.doScrollLock = true;
        scrollMarginX = 0;
        scrollMarginY = 0;
        doScrollX = true;
        doScrollY = true;
        scrollVelocity = 20.0;
        drawScrollBars = true;
    }

    public MKScrollView(int x, int y, int width, int height, boolean clipBounds) {
        this(x, y, width, height, Minecraft.getInstance().getWindow().getGuiScale(), clipBounds);
    }


    public MKScrollView setScrollVelocity(double vel) {
        scrollVelocity = vel;
        return this;
    }

    @Override
    public IMKWidget setScreen(IMKScreen screen) {
        super.setScreen(screen);
        if (screen != null) {
            screenWidth = screen.getWidth();
            screenHeight = screen.getHeight();
        }
        return this;
    }

    public MKScrollView setScrollLock(boolean state) {
        this.doScrollLock = state;
        return this;
    }

    public MKScrollView setDrawScrollBars(boolean value) {
        drawScrollBars = value;
        return this;
    }

    @Override
    public boolean onMouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double amount) {
        double dY = amount * scrollVelocity;
        if (isScrollLockOn()) {
            IMKWidget child = getChild();
            if (child != null) {
                dY = lockScrollY(child, dY);
            }
        }
        if (isContentTaller())
            offsetY += dY;
        return true;
    }

    public boolean shouldDrawScrollbars() {
        return drawScrollBars;
    }

    public boolean isClipBoundsEnabled() {
        return clipBounds;
    }

    public MKScrollView setOffsetX(double value) {
        offsetX = value;
        return this;
    }

    private int getIntOffsetX() {
        return (int) Math.round(offsetX);
    }

    private int getIntOffsetY() {
        return (int) Math.round(offsetY);
    }

    public MKScrollView setDoScrollX(boolean value) {
        doScrollX = value;
        return this;
    }

    public MKScrollView setDoScrollY(boolean value) {
        doScrollY = value;
        return this;
    }

    public boolean shouldScrollY() {
        return doScrollY;
    }

    public boolean shouldScrollX() {
        return doScrollX;
    }

    public MKScrollView setOffsetY(double value) {
        offsetY = value;
        return this;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public MKScrollView setClipBoundsEnabled(boolean value) {
        clipBounds = value;
        return this;
    }

    public boolean isScrollLockOn() {
        return doScrollLock;
    }

    public int getScrollMarginX() {
        return scrollMarginX;
    }

    public int getScrollMarginY() {
        return scrollMarginY;
    }

    public MKScrollView setScrollMarginX(int value) {
        scrollMarginX = value;
        return this;
    }

    public MKScrollView setScrollMarginY(int value) {
        scrollMarginY = value;
        return this;
    }

    public void centerContentX() {
        if (getChildren().size() > 0) {
            IMKWidget child = this.getChildren().getFirst();
            setOffsetX(getWidth() / 2.0 - child.getWidth() / 2.0);
        }
    }

    public void centerContentY() {
        if (getChildren().size() > 0) {
            IMKWidget child = this.getChildren().getFirst();
            setOffsetY(getHeight() / 2.0 - child.getHeight() / 2.0);
        }
    }

    public void setToTop() {
        setOffsetY(0);
    }

    public void setToRight() {
        setOffsetX(0);
    }

    public void resetView() {
        setToTop();
        setToRight();
    }

    @Override
    public void preDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        matrixStack.pushPose();
        matrixStack.translate(getIntOffsetX(), getIntOffsetY(), 0);
    }


    @Override
    public void draw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isClipBoundsEnabled()) {
            int y1 = screenHeight - y - height - 1;
            RenderSystem.enableScissor((int) Math.round(x * scaleFactor), (int) Math.round(y1 * scaleFactor),
                    (int) Math.round(width * scaleFactor), (int) Math.round((height + 1) * scaleFactor));
        }
    }

    public boolean isContentWider() {
        IMKWidget child = getChild();
        if (child == null) {
            return false;
        }
        return child.getWidth() > getWidth();
    }

    public boolean isContentTaller() {
        IMKWidget child = getChild();
        if (child == null) {
            return false;
        }
        return child.getHeight() > getHeight();
    }

    @Override
    public void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isClipBoundsEnabled()) {
            RenderSystem.disableScissor();
        }
        matrixStack.popPose();
        if (shouldDrawScrollbars()) {
            IMKWidget child = getChild();
            if (child == null) {
                return;
            }
            if (isContentTaller()) {
                float ratio = (float) getHeight() / (float) child.getHeight();
                int heightForScrollbar = Math.round(ratio * getHeight()) + 1;
                float posRatio = (float) (-getOffsetY()) / (float) child.getHeight();
                int pos = (int) (posRatio * getHeight());
                int barX = getX() + getWidth() - SCROLL_BAR_WIDTH;
                int barY = getY() + pos;
                mkFill(matrixStack, barX, barY, barX + SCROLL_BAR_WIDTH,
                        barY + heightForScrollbar, 0x7DFFFFFF);
            }
            if (isContentWider()) {
                float ratio = (float) getWidth() / (float) child.getWidth();
                int widthForScrollbar = (int) (ratio * getWidth());
                float posRatio = (float) (getX() - getOffsetX()) / (float) child.getWidth();
                int pos = Math.round(posRatio * getWidth());
                int barX = getX() + pos;
                int barY = getY() + getHeight() - SCROLL_BAR_WIDTH;
                mkFill(matrixStack, barX, barY, barX + widthForScrollbar,
                        barY + SCROLL_BAR_WIDTH, 0x7DFFFFFF);
            }
        }
    }

    @Override
    public Vec2i getParentCoords(Vec2i pos) {
        if (getParent() == null) {
            return pos.add(getIntOffsetX(), getIntOffsetY());
        } else {
            return getParent().getParentCoords(pos.add(getIntOffsetX(), getIntOffsetY()));
        }
    }

    @Override
    public void mouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!checkHovered(mouseX, mouseY)) {
            clearHovered();
            return;
        }
        for (IMKWidget child : getChildren()) {
            if (child.isVisible() && child.isEnabled()) {
                child.mouseHover(mc, mouseX - getIntOffsetX(), mouseY - getIntOffsetY(), partialTicks);
            }
        }
        onMouseHover(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawChildren(PoseStack matrixStack, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (IMKWidget child : getChildren()) {
            if (child.isVisible()) {
                child.drawWidget(matrixStack, mc, mouseX - getIntOffsetX(), mouseY - getIntOffsetY(), partialTicks);
            }
        }
    }


    @Override
    public boolean mousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (!this.isEnabled() || !this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mousePressed(minecraft, mouseX - offsetX, mouseY - offsetY, mouseButton)) {
                return true;
            }
        }
        return onMousePressed(minecraft, mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton, double dX, double dY) {
        if (!this.isEnabled() || !this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mouseDragged(minecraft, mouseX - offsetX, mouseY - offsetY, mouseButton, dX, dY)) {
                return true;
            }
        }
        return onMouseDragged(minecraft, mouseX, mouseY, mouseButton, dX, dY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (!this.isEnabled() || !this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mouseReleased(mouseX - offsetX, mouseY - offsetY, mouseButton)) {
                return true;
            }
        }
        return onMouseRelease(mouseX, mouseY, mouseButton);
    }


    @Nullable
    public IMKWidget getChild() {
        if (getChildren().size() > 0) {
            return this.getChildren().getFirst();
        } else {
            return null;
        }
    }

    @Override
    public boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton, double dX, double dY) {
        if (isDragging) {
            IMKWidget child = getChild();
            if (isScrollLockOn() && child != null) {
                dX = lockScrollX(child, dX);
                dY = lockScrollY(child, dY);
            }
            if (doScrollX && isContentWider()) {
                offsetX += dX;
            }
            if (doScrollY && isContentTaller()) {
                offsetY += dY;
            }
            return true;
        }
        return false;
    }


    public double lockScrollX(IMKWidget child, double dX) {
        int scrollX = 0;
        int childWidth = child.getWidth();
        int scrollWidth = getWidth();
//        Log.info("ScrollX : %d, childWidth : %d, scrollWidth : %d, cameraX: %d, dX : %d", scrollX, childWidth, scrollWidth, offsetX, dX);
        if (childWidth <= scrollWidth) {
            if (offsetX + dX < scrollX) {
                dX = scrollX - offsetX;
            } else if (offsetX + dX + childWidth > scrollX + scrollWidth) {
                dX = scrollX + scrollWidth - offsetX - childWidth;
            }
        } else {
//            if (offsetX + dX + childWidth < scrollX + getScrollMarginX()) {
//                dX = scrollX + getScrollMarginX() - offsetX - childWidth;
//            } else if (offsetX + dX > scrollX + scrollWidth - getScrollMarginX()) {
//                dX = scrollX + scrollWidth - getScrollMarginX() - offsetX;
//            }
            // top of child passes top of scroll view
            if (offsetX + dX - getScrollMarginX() > 0 && dX > 0) {
                dX = -offsetX + getScrollMarginX();
                // bottom of child passes bottom of scroll view
            } else if (offsetX + dX + childWidth + getScrollMarginX() < scrollWidth && dX < 0) {
                dX = scrollWidth - offsetX - childWidth - getScrollMarginX();
            }
        }
        return dX;
    }

    public double lockScrollY(IMKWidget child, double dY) {
        int scrollY = 0;
        int childHeight = child.getHeight();
        int scrollHeight = getHeight();
        if (childHeight <= scrollHeight) {
            if (offsetY + dY < scrollY) {
                dY = scrollY - offsetY;
            } else if (offsetY + dY + childHeight > scrollY + scrollHeight) {
                dY = scrollY + scrollHeight - offsetY - childHeight;
            }
        } else {
            // bottom of child passes top of scroll view
//            if (offsetY + dY + childHeight < -getScrollMarginY()) {
//                dY = -getScrollMarginY() - offsetY - childHeight;
//                // top of child passes bottom of scroll view
//            } else if (offsetY + dY > scrollHeight + getScrollMarginY()) {
//                dY = scrollHeight + getScrollMarginY() - offsetY;
//                // top of child passes top of scroll view
            if (offsetY + dY - getScrollMarginY() > 0 && dY > 0) {
                dY = -offsetY + getScrollMarginY();
                // bottom of child passes bottom of scroll view
            } else if (offsetY + dY + childHeight + getScrollMarginY() < scrollHeight && dY < 0) {
                dY = scrollHeight - offsetY - childHeight - getScrollMarginY();
            }
        }
        return dY;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        isDragging = true;
        return true;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (isDragging) {
            isDragging = false;
            return true;
        }
        return false;
    }


    @Override
    public boolean addWidget(IMKWidget widget) {
        if (getChildren().size() > 0) {
            return false;
        }
        widget.setX(getX());
        widget.setY(getY());
        return super.addWidget(widget);
    }
}
