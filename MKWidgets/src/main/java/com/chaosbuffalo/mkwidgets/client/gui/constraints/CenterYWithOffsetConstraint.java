package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

public class CenterYWithOffsetConstraint extends CenterYConstraint {

    private final int offset;

    public CenterYWithOffsetConstraint(int offset) {
        this.offset = offset;
    }

    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        int availableHeight = this.getAvailableHeight(layout);
        int extra = (availableHeight - widget.getHeight()) / 2;
        int newY = layout.getY() + layout.getMarginTop() + extra + offset;
        widget.setY(newY);
    }
}
