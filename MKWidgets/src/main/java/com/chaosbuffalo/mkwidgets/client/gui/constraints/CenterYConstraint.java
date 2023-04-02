package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

public class CenterYConstraint extends BaseConstraint {

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        int availableHeight = getAvailableHeight(layout);
        int extra = (availableHeight - widget.getHeight()) / 2;
        int newY = layout.getY() + layout.getMarginTop() + extra;
        widget.setY(newY);
    }
}
