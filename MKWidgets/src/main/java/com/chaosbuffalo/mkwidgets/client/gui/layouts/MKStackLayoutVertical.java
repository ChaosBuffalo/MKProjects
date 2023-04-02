package com.chaosbuffalo.mkwidgets.client.gui.layouts;


import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

public class MKStackLayoutVertical extends MKLayout {

    private int currentHeight;
    private boolean doSetWidth;

    public MKStackLayoutVertical(int x, int y, int width) {
        super(x, y, width, 0);
        currentHeight = 0;
        doSetWidth = false;
    }

    public MKStackLayoutVertical doSetChildWidth(boolean value) {
        doSetWidth = value;
        return this;
    }

    public boolean shouldSetChildWidth() {
        return doSetWidth;
    }

    @Override
    public void preLayout() {
        currentHeight = 0;
        currentHeight += getMarginBot() + getMarginTop();
    }

    @Override
    public void postLayoutWidget(IMKWidget widget, int index) {
        super.postLayoutWidget(widget, index);
        if (index > 0) {
            currentHeight += getPaddingBot() + getPaddingTop();
        }
        currentHeight += widget.getHeight();
    }

    @Override
    public void postLayout() {
        skipComputeSetHeight(currentHeight);
    }

    @Override
    public boolean addWidget(IMKWidget widget) {
        super.addWidget(widget);
        addConstraintToWidget(MarginConstraint.LEFT, widget);
        addConstraintToWidget(StackConstraint.VERTICAL, widget);
        if (shouldSetChildWidth()) {
            addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), widget);
        }
        return true;
    }
}
