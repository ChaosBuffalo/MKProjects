package com.chaosbuffalo.mkwidgets.client.gui.layouts;


import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeHeightConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

/**
 * Horizontal stack layout that places each child after the previous one and grows its own width to fit.
 */
public class MKStackLayoutHorizontal extends MKLayout {
    private int currentWidth;
    private boolean doSetHeight;

    /**
     * Creates a horizontal stack with a fixed height and computed width.
     *
     * @param x left position
     * @param y top position
     * @param height fixed layout height
     */
    public MKStackLayoutHorizontal(int x, int y, int height) {
        super(x, y, 0, height);
        currentWidth = 0;
        doSetHeight = false;
    }

    /**
     * Configures whether children should automatically fill the stack's available height.
     *
     * @param value {@code true} to apply a full-height relative constraint to future children
     * @return this layout
     */
    public MKStackLayoutHorizontal doSetChildHeight(boolean value) {
        doSetHeight = value;
        return this;
    }

    public boolean shouldSetChildHeight() {
        return doSetHeight;
    }

    @Override
    public void preLayout() {
        currentWidth = 0;
        currentWidth += getMarginLeft() + getMarginRight();
    }

    @Override
    public void postLayoutWidget(IMKWidget widget, int index) {
        super.postLayoutWidget(widget, index);
        if (index > 0) {
            currentWidth += getPaddingLeft() + getPaddingRight();
        }
        currentWidth += widget.getWidth();
    }

    @Override
    public void postLayout() {
        skipComputeSetWidth(currentWidth);
    }

    @Override
    public boolean addWidget(IMKWidget widget) {
        super.addWidget(widget);
        addConstraintToWidget(MarginConstraint.TOP, widget);
        addConstraintToWidget(StackConstraint.HORIZONTAL, widget);
        if (shouldSetChildHeight()) {
            addConstraintToWidget(new LayoutRelativeHeightConstraint(1.0f), widget);
        }
        return true;
    }
}
