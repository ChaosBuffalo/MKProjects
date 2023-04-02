package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

import java.util.UUID;

public enum MarginConstraint implements IConstraint {
    TOP {
        @Override
        public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
            widget.setY(layout.getTop() + layout.getMarginTop());
        }
    },
    BOTTOM {
        @Override
        public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
            widget.setY(layout.getBottom() - layout.getMarginBot() - widget.getHeight());
        }
    },
    LEFT {
        @Override
        public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
            widget.setX(layout.getLeft() + layout.getMarginLeft());
        }
    },
    RIGHT {
        @Override
        public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
            widget.setX(layout.getRight() - layout.getMarginRight() - widget.getWidth());
        }
    };

    private final UUID constraintUUID;

    MarginConstraint() {
        constraintUUID = UUID.randomUUID();
    }

    @Override
    public UUID getConstraintID() {
        return constraintUUID;
    }
}
