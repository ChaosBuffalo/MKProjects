package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;

import javax.annotation.Nullable;

public interface IAbilityScreen extends IMKScreen {
    boolean allowsDraggingAbilities();

    void startDraggingAbility(AbilityUiEntry dragging);

    @Nullable
    AbilityUiEntry getSelectedAbility();

    void setSelectedAbility(@Nullable AbilityUiEntry ability);

    void stopDraggingAbility();

    boolean isDraggingAbility();

    @Nullable
    AbilityUiEntry getDraggingAbility();
}
