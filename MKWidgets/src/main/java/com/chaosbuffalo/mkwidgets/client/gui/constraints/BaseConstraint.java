package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;

import java.util.UUID;

/**
 * Convenience base class for constraints that need a generated identity and helpers for usable layout space.
 */
public abstract class BaseConstraint implements IConstraint {
    private final UUID constraintId;

    /**
     * Creates a new constraint instance with its own removal identifier.
     */
    public BaseConstraint() {
        constraintId = UUID.randomUUID();
    }

    /**
     * Returns the width available to children after margins and inter-child padding are accounted for.
     *
     * @param layout layout being measured
     * @return usable child width
     */
    public int getAvailableWidth(IMKLayout layout) {
        int availableSpace = layout.getWidth() - layout.getMarginRight() - layout.getMarginLeft();
        int numChildren = layout.getChildren().size();
        if (numChildren > 1) {
            availableSpace -= (layout.getPaddingLeft() + layout.getPaddingRight()) * (numChildren - 1);
        }
        return availableSpace;
    }

    /**
     * Returns the height available to children after margins and inter-child padding are accounted for.
     *
     * @param layout layout being measured
     * @return usable child height
     */
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
