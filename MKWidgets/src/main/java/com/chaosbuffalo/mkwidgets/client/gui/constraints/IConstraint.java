package com.chaosbuffalo.mkwidgets.client.gui.constraints;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.IMKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

import java.util.UUID;

public interface IConstraint {

    void applyConstraint(IMKLayout layout, IMKWidget widget, int widgetIndex);

    UUID getConstraintID();
}
