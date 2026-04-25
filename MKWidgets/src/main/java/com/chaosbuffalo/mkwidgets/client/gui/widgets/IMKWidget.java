package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Base contract for every node in the MKWidgets widget tree.
 * <p>
 * An {@code IMKWidget} owns bounds, visibility, hover state, optional focus behavior, and zero or more child
 * widgets. Rendering and input are both dispatched recursively through this tree, which means composition
 * determines not just what is drawn but also how mouse and keyboard interaction is routed.
 * <p>
 * Most concrete widgets extend {@link MKWidget}, while containers such as
 * {@link com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout} and
 * {@link com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView} customize how child widgets are
 * positioned or how coordinates are transformed during dispatch.
 */
public interface IMKWidget {

    /**
     * Adds a child widget to this widget and attaches its parent/screen context.
     *
     * @param widget child widget to add
     * @return {@code true} if the widget was accepted
     */
    default boolean addWidget(IMKWidget widget) {
        widget.setParent(this);
        widget.inheritScreen(getScreen());
        getChildren().add(widget);
        return true;
    }

    /**
     * Propagates the given screen attachment through this widget subtree.
     *
     * @param screen owning screen, or {@code null} if detaching
     */
    default void inheritScreen(IMKScreen screen) {
        setScreen(screen);
        for (IMKWidget widget : getChildren()) {
            widget.inheritScreen(screen);
        }
    }

    /**
     * Removes a direct child widget from this widget.
     *
     * @param widget child widget to remove
     */
    default void removeWidget(IMKWidget widget) {
        if (widget.getParent() != null && widget.getParent().getId().equals(this.getId())) {
            getChildren().removeIf((x) -> x.getId().equals(widget.getId()));
            widget.setParent(null);
            widget.inheritScreen(null);
        }
    }

    /**
     * Collects all focusable widgets in this subtree.
     *
     * @param tree destination list populated in tree order
     */
    default void findFocusable(List<IMKWidget> tree) {
        if (canFocus()) {
            tree.add(this);
        }
        for (IMKWidget wid : getChildren()) {
            wid.findFocusable(tree);
        }
    }

    /**
     * @return stable identifier for this widget instance
     */
    UUID getId();

    /**
     * Returns this widget's direct children in draw order.
     *
     * @return the mutable child list owned by this widget
     */
    LinkedList<IMKWidget> getChildren();

    /**
     * Sets this widget's parent pointer.
     *
     * @param parent parent widget, or {@code null} if detached
     * @return this widget
     */
    IMKWidget setParent(IMKWidget parent);

    /**
     * Sets the screen this widget is attached to.
     *
     * @param screen owning screen, or {@code null} if detached
     * @return this widget
     */
    IMKWidget setScreen(IMKScreen screen);

    @Nullable
    /**
     * @return direct parent widget, or {@code null} for roots
     */
    IMKWidget getParent();

    @Nullable
    /**
     * @return owning screen, or {@code null} if detached
     */
    IMKScreen getScreen();

    /**
     * Sets widget height.
     *
     * @param newHeight new height in pixels
     * @return this widget
     */
    IMKWidget setHeight(int newHeight);

    /**
     * Sets widget width.
     *
     * @param newWidth new width in pixels
     * @return this widget
     */
    IMKWidget setWidth(int newWidth);

    /**
     * Sets widget x position.
     *
     * @param newX new left coordinate
     * @return this widget
     */
    IMKWidget setX(int newX);

    /**
     * Sets widget y position.
     *
     * @param newY new top coordinate
     * @return this widget
     */
    IMKWidget setY(int newY);

    /**
     * @return widget width in pixels
     */
    int getWidth();

    /**
     * @return widget height in pixels
     */
    int getHeight();

    /**
     * @return widget left coordinate
     */
    int getX();

    /**
     * @return widget top coordinate
     */
    int getY();

    /**
     * @return right edge coordinate
     */
    default int getRight() {
        return getX() + getWidth();
    }

    /**
     * @return top edge coordinate
     */
    default int getTop() {
        return getY();
    }

    /**
     * @return left edge coordinate
     */
    default int getLeft() {
        return getX();
    }

    /**
     * @return bottom edge coordinate
     */
    default int getBottom() {
        return getY() + getHeight();
    }

    /**
     * Converts a local position into ancestor/root space by walking up the widget tree.
     *
     * @param pos local position
     * @return translated position in parent/root coordinates
     */
    default Vec2i getParentCoords(Vec2i pos) {
        if (getParent() == null) {
            return pos;
        } else {
            return getParent().getParentCoords(pos);
        }
    }

    /**
     * @return hover duration threshold before long-hover behavior triggers
     */
    int getLongHoverTicks();

    /**
     * @return {@code true} if this widget can receive focus
     */
    default boolean canFocus() {
        return false;
    }

    /**
     * Hook invoked when this widget receives focus.
     */
    default void onFocus() {
    }

    /**
     * Hook invoked when this widget loses focus.
     */
    default void onFocusLost() {
    }

    /**
     * Sets the hover threshold used for long-hover behavior.
     *
     * @param ticks hover threshold in ticks
     * @return this widget
     */
    IMKWidget setLongHoverTicks(int ticks);

    /**
     * @return {@code true} if bounds checks should be bypassed during hit testing
     */
    boolean skipBoundsCheck();

    /**
     * Enables or disables bounds-free hit testing.
     *
     * @param value {@code true} to bypass bounds checks
     * @return this widget
     */
    IMKWidget setSkipBoundsCheck(boolean value);

    /**
     * @return {@code true} if this widget is currently hovered
     */
    boolean isHovered();

    /**
     * Sets the current hovered state.
     *
     * @param value new hovered state
     * @return this widget
     */
    IMKWidget setHovered(boolean value);

    /**
     * @return {@code true} if this widget is visible
     */
    boolean isVisible();

    /**
     * Updates widget visibility.
     *
     * @param value new visibility state
     * @return this widget
     */
    IMKWidget setVisible(boolean value);

    /**
     * @return {@code true} if this widget responds to input
     */
    boolean isEnabled();

    /**
     * Updates widget enabled state.
     *
     * @param value new enabled state
     * @return this widget
     */
    IMKWidget setEnabled(boolean value);

    /**
     * Tests whether a point lies inside the widget's interactive bounds.
     *
     * @param x x coordinate to test
     * @param y y coordinate to test
     * @return {@code true} if the point is inside the widget or bounds checks are skipped
     */
    default boolean isInBounds(double x, double y) {
        if (skipBoundsCheck()) {
            return true;
        }
        return x >= getX() && y >= getY() && x < getRight() && y < getBottom();
    }

    /**
     * First hook in the widget draw lifecycle.
     * <p>
     * Override this to prepare render state before the widget draws itself or its children. Typical uses
     * include pushing transforms, enabling clipping, or setting up transient drawing state that should affect
     * descendants.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param x widget x position
     * @param y widget y position
     * @param width widget width
     * @param height widget height
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void preDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }

    /**
     * Draws this widget's own visual contents.
     * <p>
     * This runs after {@link #preDraw(GuiGraphics, Minecraft, int, int, int, int, int, int, float)} and before
     * {@link #drawChildren(GuiGraphics, Minecraft, int, int, float)}. Override this for the widget's primary
     * rendering logic.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param x widget x position
     * @param y widget y position
     * @param width widget width
     * @param height widget height
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }

    /**
     * Final hook in the widget draw lifecycle.
     * <p>
     * Override this to clean up state established in {@link #preDraw(GuiGraphics, Minecraft, int, int, int, int,
     * int, int, float)} or to render overlays that should appear above this widget's children but still be part
     * of the widget's normal draw pass.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param x widget x position
     * @param y widget y position
     * @param width widget width
     * @param height widget height
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void postDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {

    }

    /**
     * Draws long-hover content for this widget.
     * <p>
     * This is typically used for tooltips or similar hover affordances. It is invoked by
     * {@link #handleLongHoverDraw(GuiGraphics, Minecraft, int, int, int, int, int, int, float)} after the
     * normal widget draw pipeline has completed.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param x widget x position
     * @param y widget y position
     * @param width widget width
     * @param height widget height
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void longHoverDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                               int mouseX, int mouseY, float partialTicks) {

    }

    /**
     * @return accumulated hover time for the current hover session
     */
    float getHoveredTicks();

    /**
     * Sets the accumulated hover time.
     *
     * @param value hover time value
     */
    void setHoveredTicks(float value);

    /**
     * Returns whether this widget should be treated as hovered for the given mouse position.
     *
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     * @return {@code true} if the widget is visible, enabled, and in bounds
     */
    default boolean checkHovered(int mouseX, int mouseY) {
        return isVisible() && isEnabled() && isInBounds(mouseX, mouseY);
    }

    /**
     * Invokes {@link #longHoverDraw(GuiGraphics, Minecraft, int, int, int, int, int, int, float)} when the
     * widget has remained hovered for longer than {@link #getLongHoverTicks()}.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param x widget x position
     * @param y widget y position
     * @param width widget width
     * @param height widget height
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void handleLongHoverDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                     int mouseX, int mouseY, float partialTicks) {
        if (isHovered() && getHoveredTicks() > getLongHoverTicks()) {
            longHoverDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Convenience overload that sets a tooltip from plain text.
     *
     * @param newTooltip tooltip text
     * @return this widget
     */
    default IMKWidget setTooltip(String newTooltip) {
        return setTooltip(Component.literal(newTooltip));
    }

    /**
     * Sets the tooltip shown by the default long-hover implementation.
     *
     * @param text tooltip content
     * @return this widget
     */
    IMKWidget setTooltip(Component text);

    /**
     * Removes any tooltip attached to this widget.
     */
    void clearTooltip();

    /**
     * Updates hovered state and hover time accumulation for the current frame.
     *
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     * @param partialTicks current partial tick value
     */
    default void handleHoverDetection(int mouseX, int mouseY, float partialTicks) {
        boolean hovered = checkHovered(mouseX, mouseY);
        if (hovered) {
            setHoveredTicks(getHoveredTicks() + partialTicks);
        } else {
            setHoveredTicks(0);
        }
        setHovered(hovered);
    }


    /**
     * Clears hovered state for this widget and its descendants.
     */
    default void clearHovered() {
        setHoveredTicks(0);
        setHovered(false);
        for (IMKWidget child : getChildren()) {
            child.clearHovered();
        }
    }

    /**
     * @return {@code true} when debug bounds should be rendered during {@link #drawWidget}
     */
    boolean doDrawDebugBounds();

    /**
     * Draws a debug visualization for this widget's bounds.
     * <p>
     * This is called from {@link #drawWidget(GuiGraphics, Minecraft, int, int, float)} before the normal draw
     * lifecycle whenever {@link #doDrawDebugBounds()} returns {@code true}. Override it to customize how debug
     * bounds are displayed.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param x widget x position
     * @param y widget y position
     * @param width widget width
     * @param height widget height
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void drawDebugBounds(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX,
                                 int mouseY, float partialTicks) {

    }

    /**
     * Enables or disables debug bounds rendering.
     *
     * @param value {@code true} to draw debug bounds
     */
    void setDrawDebug(boolean value);

    /**
     * Sets the debug bounds color.
     *
     * @param color packed ARGB debug color
     */
    void setDebugColor(int color);

    /**
     * @return debug bounds color
     */
    int getDebugColor();

    /**
     * Hover hook invoked after child hover processing.
     */
    default void onMouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        handleHoverDetection(mouseX, mouseY, partialTicks);
    }

    /**
     * Recursively updates hover state for this widget subtree.
     */
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

    /**
     * Recursively draws visible child widgets in child list order.
     * <p>
     * Override this when a container needs to transform child coordinates, clip child rendering, or otherwise
     * customize subtree drawing while still preserving the parent widget's own lifecycle.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void drawChildren(GuiGraphics graphics, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        for (IMKWidget child : getChildren()) {
            if (child.isVisible()) {
                child.drawWidget(graphics, mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    /**
     * Draws this widget and then recursively draws its child subtree.
     * <p>
     * This method defines the standard MKWidgets hierarchical draw order:
     * {@code debug bounds -> preDraw -> draw -> drawChildren -> postDraw -> long-hover draw}.
     * Container widgets typically inherit this behavior and override one or more lifecycle hooks rather than
     * replacing the whole method.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param mouseX current mouse x coordinate in the widget's expected input space
     * @param mouseY current mouse y coordinate in the widget's expected input space
     * @param partialTicks current partial tick value
     */
    default void drawWidget(GuiGraphics graphics, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        if (doDrawDebugBounds()) {
            drawDebugBounds(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        }
        preDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        draw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        drawChildren(graphics, mc, mouseX, mouseY, partialTicks);
        postDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        handleLongHoverDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
    }

    /**
     * Removes and detaches all child widgets.
     */
    default void clearWidgets() {
        for (IMKWidget widget : getChildren()) {
            widget.setParent(null);
        }
        getChildren().clear();
    }

    @Nullable
    /**
     * Returns a child widget by index.
     *
     * @param index child index
     * @return child widget
     */
    default IMKWidget getChild(int index) {
        return getChildren().get(index);
    }

    /**
     * Widget-specific mouse wheel hook invoked after child dispatch.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean onMouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double pScrollX, double pScrollY) {
        return false;
    }

    /**
     * Recursively dispatches mouse wheel input through this widget subtree.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean mouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double pScrollX, double pScrollY) {
        if (!this.isVisible() || !this.isInBounds(mouseX, mouseY)) {
            return false;
        }
        if (!this.isEnabled()) {
            return true;
        }
        Iterator<IMKWidget> it = getChildren().descendingIterator();
        while (it.hasNext()) {
            IMKWidget child = it.next();
            if (child.mouseScrollWheel(minecraft, mouseX, mouseY, pScrollX, pScrollY)) {
                return true;
            }
        }
        return onMouseScrollWheel(minecraft, mouseX, mouseY, pScrollX, pScrollY);
    }

    /**
     * Hook invoked when a drag sequence that originated from this widget ends.
     *
     * @param state final drag state
     */
    default void onDragEnd(IDragState state) {

    }

    /**
     * Recursively dispatches mouse drag input through this widget subtree.
     *
     * @return {@code true} if the event was consumed
     */
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

    /**
     * Widget-specific mouse drag hook invoked after child dispatch.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton,
                                   double dX, double dY) {
        return false;
    }

    /**
     * Recursively dispatches mouse release input through this widget subtree.
     *
     * @return {@code true} if the event was consumed
     */
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

    /**
     * Widget-specific mouse release hook invoked after child dispatch.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    /**
     * Recursively dispatches mouse press input through this widget subtree.
     * <p>
     * If this widget consumes the press, focus is updated on the owning screen according to
     * {@link #canFocus()}.
     *
     * @return {@code true} if the event was consumed
     */
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

    /**
     * Widget-specific mouse press hook invoked after child dispatch.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    /**
     * Handles key press input for this widget.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean keyPressed(Minecraft minecraft, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Handles key release input for this widget.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean keyReleased(Minecraft minecraft, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Handles typed character input for this widget.
     *
     * @return {@code true} if the event was consumed
     */
    default boolean charTyped(Minecraft minecraft, char codePoint, int modifiers) {
        return false;
    }
}
