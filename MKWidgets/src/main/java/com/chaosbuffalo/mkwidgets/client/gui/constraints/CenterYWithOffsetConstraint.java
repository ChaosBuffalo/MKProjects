package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

/**
 * Variant of {@link CenterYConstraint} that applies an additional vertical offset after centering.
 */
public class CenterYWithOffsetConstraint extends CenterYConstraint {

    private final int offset;

    /**
     * Creates a centered vertical constraint with an additional y offset.
     *
     * @param offset extra pixels to add after centering
     */
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
