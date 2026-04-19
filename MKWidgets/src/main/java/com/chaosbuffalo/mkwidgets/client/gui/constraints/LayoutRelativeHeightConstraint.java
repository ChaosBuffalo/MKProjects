package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

/**
 * Sets widget height as a fraction of the layout's usable height.
 */
public class LayoutRelativeHeightConstraint extends BaseConstraint {
    private final float heightScale;

    /**
     * @param heightScale fraction of available layout height to assign to the widget
     */
    public LayoutRelativeHeightConstraint(float heightScale) {
        super();
        this.heightScale = heightScale;
    }

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        float availableSpace = getAvailableHeight(layout);
        widget.setHeight(Math.round(availableSpace * heightScale));
    }
}
