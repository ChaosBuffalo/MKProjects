package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;

public interface IAbilityScreen extends IMKScreen {
    boolean allowsDraggingAbilities();

    void startDraggingAbility(MKAbility dragging);

    MKAbility getSelectedAbility();

    void setSelectedAbility(MKAbility ability);

    void stopDraggingAbility();

    boolean isDraggingAbility();

    MKAbility getDraggingAbility();
}
