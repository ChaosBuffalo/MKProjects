package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;

import java.util.UUID;

public abstract class BaseConstraint implements IConstraint {
    private final UUID constraintId;

    public BaseConstraint() {
        constraintId = UUID.randomUUID();
    }

    public int getAvailableWidth(IMKLayout layout) {
        int availableSpace = layout.getWidth() - layout.getMarginRight() - layout.getMarginLeft();
        int numChildren = layout.getChildren().size();
        if (numChildren > 1) {
            availableSpace -= (layout.getPaddingLeft() + layout.getPaddingRight()) * (numChildren - 1);
        }
        return availableSpace;
    }

    public int getAvailableHeight(IMKLayout layout) {
        int availableSpace = layout.getHeight() - layout.getMarginTop() - layout.getMarginBot();
        int numChildren = layout.getChildren().size();
        if (numChildren > 1) {
            availableSpace -= (layout.getPaddingTop() + layout.getPaddingBot()) * (numChildren - 1);
        }
        return availableSpace;
    }

    @Override
    public UUID getConstraintID() {
        return constraintId;
    }
}
