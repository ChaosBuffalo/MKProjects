package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;

public interface IAbilityScreen extends IMKScreen {

    MKAbilityInfo getSelectedAbility();

    void setSelectedAbility(MKAbilityInfo ability);

    boolean allowsDraggingAbilities();

    void startDraggingAbility(MKAbilityInfo ability, MKImage icon, IMKWidget source);

    void stopDraggingAbility();

    default boolean isDraggingAbility() {
        return getDraggingAbility() != null;
    }

    MKAbilityInfo getDraggingAbility();
}
