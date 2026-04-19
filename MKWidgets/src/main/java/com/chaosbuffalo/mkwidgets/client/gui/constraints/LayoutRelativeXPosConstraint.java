package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

/**
 * Positions a widget at a fractional x offset inside the layout's usable width.
 */
public class LayoutRelativeXPosConstraint extends BaseConstraint {
    private final float xScale;

    /**
     * @param xScale fraction of usable width to offset from the layout's left margin
     */
    public LayoutRelativeXPosConstraint(float xScale) {
        super();
        this.xScale = xScale;
    }

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        int usableWidth = getAvailableWidth(layout);
        int scaledX = Math.round(usableWidth * xScale);
        int xPos = layout.getX() + layout.getMarginLeft() + scaledX;
        widget.setX(xPos);
    }
}
