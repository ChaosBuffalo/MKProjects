package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;

public interface IAbilityScreen extends IMKScreen {
    MKAbility getSelectedAbility();

    void setSelectedAbility(MKAbility ability);

    boolean allowsDraggingAbilities();

    void startDraggingAbility(MKAbility ability, MKImage icon, IMKWidget source);

    void stopDraggingAbility();

    default boolean isDraggingAbility() {
        return getDraggingAbility() != null;
    }

    MKAbility getDraggingAbility();
}
