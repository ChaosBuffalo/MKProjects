package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;


/**
 * Sets widget width as a fraction of the layout's usable width.
 */
public class LayoutRelativeWidthConstraint extends BaseConstraint {
    private final float widthScale;

    /**
     * @param widthScale fraction of available layout width to assign to the widget
     */
    public LayoutRelativeWidthConstraint(float widthScale) {
        super();
        this.widthScale = widthScale;
    }

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        float availableSpace = getAvailableWidth(layout);
        widget.setWidth(Math.round(availableSpace * widthScale));
    }
}
