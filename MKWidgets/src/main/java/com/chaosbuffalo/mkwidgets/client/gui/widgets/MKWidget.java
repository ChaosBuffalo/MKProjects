package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.UIConstants;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Default concrete implementation of {@link IMKWidget}.
 * <p>
 * {@code MKWidget} provides the shared state for most MKWidgets controls: parent/child ownership, screen
 * attachment, bounds, hover timing, visibility and enabled flags, optional focus support, tooltip handling,
 * and debug rendering.
 */
public class MKWidget implements IMKWidget {
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

    /**
     * Creates a widget with explicit bounds and no parent or screen attachment.
     *
     * @param x left position
     * @param y top position
     * @param width widget width
     * @param height widget height
     */
    public MKWidget(int x, int y, int width, int height) {
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

    /**
     * Enables or disables participation in screen focus traversal.
     *
     * @param canFocus {@code true} if this widget can receive focus
     */
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

    /**
     * Updates this widget's parent reference.
     *
     * @param parent parent widget, or {@code null} if detached
     * @return this widget
     */
    @Override
    public IMKWidget setParent(IMKWidget parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Enables or disables debug bounds drawing for this widget.
     *
     * @param value {@code true} to render debug bounds
     */
    @Override
    public void setDrawDebug(boolean value) {
        drawDebug = value;
    }

    /**
     * Default long-hover behavior that schedules this widget's tooltip for post-render drawing.
     */
    @Override
    public void longHoverDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        IMKScreen screen = getScreen();
        if (tooltip != null && screen != null) {
            // tooltips are added in screen space so we need to climb the widget tree to the top.
            Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
            screen.addPostRenderInstruction(new HoveringTextInstruction(tooltip, parentPos));
        }
    }

    /**
     * Updates the screen attachment for this widget.
     *
     * @param screen owning screen, or {@code null} if detached
     * @return this widget
     */
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

    /**
     * Sets the color used by the default debug bounds renderer.
     *
     * @param color packed ARGB color
     */
    @Override
    public void setDebugColor(int color) {
        debugColor = color;
    }

    @Override
    public int getDebugColor() {
        return debugColor;
    }

    /**
     * Default debug bounds renderer that fills the widget rectangle with the configured debug color.
     */
    @Override
    public void drawDebugBounds(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), getDebugColor());
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

    /**
     * Clears the tooltip used by the default long-hover implementation.
     */
    @Override
    public void clearTooltip() {
        tooltip = null;
    }


}
