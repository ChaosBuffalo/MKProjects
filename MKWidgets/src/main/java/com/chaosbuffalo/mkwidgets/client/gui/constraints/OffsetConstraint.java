package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;


/**
 * Applies a fixed pixel offset relative to the owning layout's origin.
 */
public class OffsetConstraint extends BaseConstraint {
    private final int x;
    private final int y;
    private final boolean doX;
    private final boolean doY;

    /**
     * Creates an offset constraint that may affect x, y, or both coordinates.
     *
     * @param x horizontal offset from the layout origin
     * @param y vertical offset from the layout origin
     * @param doX whether to apply the x offset
     * @param doY whether to apply the y offset
     */
    public OffsetConstraint(int x, int y, boolean doX, boolean doY) {
        this.x = x;
        this.y = y;
        this.doX = doX;
        this.doY = doY;
    }

    /**
     * Creates an offset constraint that applies both coordinates.
     *
     * @param x horizontal offset from the layout origin
     * @param y vertical offset from the layout origin
     */
    public OffsetConstraint(int x, int y) {
        this(x, y, true, true);
    }

    @Override
    public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
        if (doX) {
            widget.setX(layout.getX() + x);
        }
        if (doY) {
            widget.setY(layout.getY() + y);
        }
    }
}
