package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

import java.util.UUID;

/**
 * A single layout rule that can be attached to a child widget inside an {@link IMKLayout}.
 */
public interface IConstraint {

    /**
     * Applies this constraint to a child widget.
     *
     * @param layout the layout currently recomputing
     * @param widget the child widget being updated
     * @param widgetIndex the child's index within the layout
     */
    void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex);

    /**
     * Returns the stable identifier used to remove this constraint from a widget later.
     *
     * @return this constraint instance's identifier
     */
    UUID getConstraintID();
}
