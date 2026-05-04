package com.chaosbuffalo.mkwidgets.client.gui.layouts;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.IConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Default constraint-based layout widget.
 * <p>
 * {@code MKLayout} stores constraints per child and recomputes child geometry lazily before drawing. This
 * makes it the general-purpose container for absolute-plus-constraint arrangements, while subclasses provide
 * common specialized policies on top of the same engine.
 */
public class MKLayout extends MKWidget implements IMKLayout {
    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBot;
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBot;
    private boolean needsRecompute;
    private HashMap<UUID, ArrayList<IConstraint>> constraints;

    /**
     * Creates a layout with explicit screen-space bounds.
     *
     * @param x left position
     * @param y top position
     * @param width layout width
     * @param height layout height
     */
    public MKLayout(int x, int y, int width, int height) {
        super(x, y, width, height);
        constraints = new HashMap<>();
        needsRecompute = false;
    }

    /**
     * Marks this layout so it recomputes child geometry before the next draw.
     */
    public void flagNeedsRecompute() {
        needsRecompute = true;
    }

    /**
     * Forces nested child layouts to resolve themselves before this layout positions them.
     */
    public void computeChildLayouts() {
        for (IMKWidget child : getChildren()) {
            if (child instanceof MKLayout) {
                if (((MKLayout) child).needsRecompute) {
                    ((MKLayout) child).manualRecompute();
                }
            }
        }
    }

    /**
     * Runs a full layout pass over child widgets.
     */
    public void recomputeChildren() {
        computeChildLayouts();
        preLayout();
        int i = 0;
        for (IMKWidget child : getChildren()) {
            layoutWidget(child, i);
            // Parent constraints can move a child layout after computeChildLayouts() ran, marking it dirty again.
            // Recompute it here so its own children resolve against the final parent-assigned position.
            if (child instanceof MKLayout childLayout && childLayout.needsRecompute) {
                childLayout.manualRecompute();
            }
            i++;
        }
        postLayout();
    }

    @Override
    public void addConstraintToWidget(IConstraint constraint, IMKWidget widget) {
        ArrayList<IConstraint> widgetConstraints = constraints.computeIfAbsent(widget.getId(),
                (id) -> new ArrayList<>());
        widgetConstraints.add(constraint);
        flagNeedsRecompute();
    }

    /**
     * Removes one registered constraint from a child widget and schedules a relayout.
     */
    @Override
    public void removeConstraintFromWidget(IConstraint constraint, IMKWidget widget) {
        if (constraints.containsKey(widget.getId())) {
            ArrayList<IConstraint> widgetConstraints = constraints.get(widget.getId());
            widgetConstraints.removeIf((con) -> con.getConstraintID().equals(constraint.getConstraintID()));
            flagNeedsRecompute();
        }
    }

    /**
     * Applies all currently registered constraints for one child widget.
     *
     * @param widget child widget being updated
     * @param widgetIndex child index within the layout
     */
    public void applyConstraints(IMKWidget widget, int widgetIndex) {
        if (constraints.containsKey(widget.getId())) {
            ArrayList<IConstraint> widgetConstraints = constraints.get(widget.getId());
            for (IConstraint constraint : widgetConstraints) {
                constraint.applyConstraint(this, widget, widgetIndex);
            }
        }

    }

    @Override
    public boolean addWidget(IMKWidget widget) {
        super.addWidget(widget);
        flagNeedsRecompute();
        return true;
    }


    /**
     * Clears all child widgets and marks the layout dirty.
     */
    @Override
    public void clearWidgets() {
        super.clearWidgets();
        flagNeedsRecompute();
    }


    @Override
    public IMKWidget setWidth(int newWidth) {
        super.setWidth(newWidth);
        flagNeedsRecompute();
        return this;
    }

    @Override
    public IMKWidget setHeight(int newHeight) {
        super.setHeight(newHeight);
        flagNeedsRecompute();
        return this;
    }

    @Override
    public IMKWidget setX(int newX) {
        super.setX(newX);
        flagNeedsRecompute();
        return this;
    }

    @Override
    public IMKWidget setY(int newY) {
        super.setY(newY);
        flagNeedsRecompute();
        return this;
    }


    /**
     * Removes a child widget, clears any constraints attached to it, and schedules a relayout.
     */
    @Override
    public void removeWidget(IMKWidget widget) {
        super.removeWidget(widget);
        clearWidgetConstraints(widget);
        flagNeedsRecompute();
    }

    /**
     * Removes all constraints registered for the given child widget.
     */
    @Override
    public void clearWidgetConstraints(IMKWidget widget) {
        if (constraints.containsKey(widget.getId())) {
            constraints.remove(widget.getId());
            flagNeedsRecompute();
        }
    }

    /**
     * Immediately recomputes child geometry instead of waiting for the next draw call.
     */
    public void manualRecompute() {
        recomputeChildren();
        needsRecompute = false;
    }

    /**
     * Draws the layout after ensuring child geometry is up to date.
     * <p>
     * This preserves the normal widget draw lifecycle, but inserts a lazy recompute step before any visual
     * work occurs so child constraints are resolved against the latest layout state.
     *
     * @param graphics active GUI graphics context
     * @param mc active client instance
     * @param mouseX current mouse x coordinate
     * @param mouseY current mouse y coordinate
     * @param partialTicks current partial tick value
     */
    @Override
    public void drawWidget(GuiGraphics graphics, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (needsRecompute) {
            recomputeChildren();
            needsRecompute = false;
        }
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        if (doDrawDebugBounds()) {
            drawDebugBounds(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        }
        preDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        draw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        int i = 0;
        for (IMKWidget child : getChildren()) {
            if (child.isVisible()) {
                child.drawWidget(graphics, mc, mouseX, mouseY, partialTicks);
            }
            i++;
        }
        postDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        handleLongHoverDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
    }


    @Override
    public IMKLayout setMarginTop(int value) {
        marginTop = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getMarginTop() {
        return marginTop;
    }

    @Override
    public IMKLayout setMarginBot(int value) {
        marginBot = value;
        flagNeedsRecompute();
        return this;
    }

    /**
     * Default child layout implementation: apply constraints, then run the post-child hook.
     */
    @Override
    public void layoutWidget(IMKWidget widget, int index) {
        applyConstraints(widget, index);
        postLayoutWidget(widget, index);
    }

    /**
     * Hook for subclasses to apply additional logic after constraints have been evaluated for one child.
     *
     * @param widget the child that was just laid out
     * @param index the child's index in layout order
     */
    public void postLayoutWidget(IMKWidget widget, int index) {

    }

    /**
     * Sets layout width without marking the layout dirty.
     * <p>
     * Intended for subclasses that compute their own size during layout.
     */
    protected void skipComputeSetWidth(int newWidth) {
        super.setWidth(newWidth);
    }

    /**
     * Sets layout height without marking the layout dirty.
     * <p>
     * Intended for subclasses that compute their own size during layout.
     */
    protected void skipComputeSetHeight(int newHeight) {
        super.setHeight(newHeight);
    }

    @Override
    public int getMarginBot() {
        return marginBot;
    }

    @Override
    public IMKLayout setMarginLeft(int value) {
        marginLeft = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getMarginLeft() {
        return marginLeft;
    }

    @Override
    public IMKLayout setMarginRight(int value) {
        marginRight = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getMarginRight() {
        return marginRight;
    }

    @Override
    public IMKLayout setPaddingTop(int value) {
        paddingTop = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingTop() {
        return paddingTop;
    }

    @Override
    public IMKLayout setPaddingBot(int value) {
        paddingBot = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingBot() {
        return paddingBot;
    }

    @Override
    public IMKLayout setPaddingLeft(int value) {
        paddingLeft = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingLeft() {
        return paddingLeft;
    }

    @Override
    public IMKLayout setPaddingRight(int value) {
        paddingRight = value;
        flagNeedsRecompute();
        return this;
    }

    @Override
    public int getPaddingRight() {
        return paddingRight;
    }
}
