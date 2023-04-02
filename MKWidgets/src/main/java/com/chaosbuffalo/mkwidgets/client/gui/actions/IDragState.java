package com.chaosbuffalo.mkwidgets.client.gui.actions;

import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import net.minecraft.client.Minecraft;

public interface IDragState {

    void updateDragState(Minecraft minecraft, int mouseX, int mouseY, IMKScreen screen);
}
