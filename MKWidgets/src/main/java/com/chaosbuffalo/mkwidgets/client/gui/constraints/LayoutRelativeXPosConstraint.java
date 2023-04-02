package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

public class LayoutRelativeXPosConstraint extends BaseConstraint {
    private final float xScale;

    public LayoutRelativeXPosConstraint(float xScale){
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
