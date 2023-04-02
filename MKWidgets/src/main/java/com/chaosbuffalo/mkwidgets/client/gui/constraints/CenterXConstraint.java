package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

public class CenterXConstraint extends BaseConstraint {

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        int availableWidth = getAvailableWidth(layout);
        int extra = (availableWidth - widget.getWidth()) / 2;
        int newX = layout.getX() + layout.getMarginLeft() + extra;
        widget.setX(newX);
    }
}
