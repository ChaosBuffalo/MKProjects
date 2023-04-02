package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

import java.util.UUID;

public enum StackConstraint implements IConstraint {
    VERTICAL {
        @Override
        public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
            int yPos;
            if (widgetIndex == 0) {
                yPos = layout.getTop() + layout.getMarginTop();
            } else {
                IMKWidget previous = layout.getChild(widgetIndex - 1);
                if (previous != null) {
                    yPos = previous.getBottom() + layout.getPaddingBot() + layout.getPaddingTop();
                } else {
                    yPos = layout.getTop() + layout.getMarginTop();
                }
            }
            widget.setY(yPos);
        }
    },
    HORIZONTAL {
        @Override
        public void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex) {
            int xPos;
            if (widgetIndex == 0) {
                xPos = layout.getLeft() + layout.getMarginLeft();
            } else {
                IMKWidget previous = layout.getChild(widgetIndex - 1);
                if (previous != null) {
                    xPos = previous.getRight() + layout.getPaddingRight() + layout.getPaddingLeft();
                } else {
                    xPos = layout.getLeft() + layout.getMarginLeft();
                }
            }
            widget.setX(xPos);
        }
    };

    private final UUID constraintUUID;

    StackConstraint() {
        constraintUUID = UUID.randomUUID();
    }

    @Override
    public UUID getConstraintID() {
        return constraintUUID;
    }
}
