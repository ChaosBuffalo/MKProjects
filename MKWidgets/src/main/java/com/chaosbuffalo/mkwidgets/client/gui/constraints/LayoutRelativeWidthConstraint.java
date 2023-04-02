package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;


public class LayoutRelativeWidthConstraint extends BaseConstraint {
    private final float widthScale;

    public LayoutRelativeWidthConstraint(float widthScale){
        super();
        this.widthScale = widthScale;
    }

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        float availableSpace = getAvailableWidth(layout);
        widget.setWidth(Math.round(availableSpace * widthScale));
    }
}
