package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;

public interface IAbilityScreen extends IMKScreen {
    MKAbility getSelectedAbility();

    void setSelectedAbility(MKAbility ability);

    boolean allowsDraggingAbilities();

    void startDraggingAbility(MKAbility dragging);

    void stopDraggingAbility();

    boolean isDraggingAbility();

    MKAbility getDraggingAbility();
}
