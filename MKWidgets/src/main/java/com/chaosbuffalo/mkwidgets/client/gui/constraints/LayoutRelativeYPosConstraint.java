package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

/**
 * Positions a widget at a fractional y offset inside the layout's usable height.
 */
public class LayoutRelativeYPosConstraint extends BaseConstraint {
    private final float yScale;

    /**
     * @param yScale fraction of usable height to offset from the layout's top margin
     */
    public LayoutRelativeYPosConstraint(float yScale) {
        super();
        this.yScale = yScale;
    }

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        int usableHeight = getAvailableHeight(layout);
        int scaledY = Math.round(usableHeight * yScale);
        int yPos = layout.getY() + layout.getMarginTop() + scaledY;
        widget.setY(yPos);
    }
}
