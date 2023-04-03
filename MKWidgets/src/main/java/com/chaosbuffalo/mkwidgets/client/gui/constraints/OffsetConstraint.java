package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;


public class OffsetConstraint extends BaseConstraint {
    private final int x;
    private final int y;
    private final boolean doX;
    private final boolean doY;

    public OffsetConstraint(int x, int y, boolean doX, boolean doY) {
        this.x = x;
        this.y = y;
        this.doX = doX;
        this.doY = doY;
    }

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
