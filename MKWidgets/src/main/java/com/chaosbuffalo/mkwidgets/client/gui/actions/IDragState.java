package com.chaosbuffalo.mkwidgets.client.gui.actions;

import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import net.minecraft.client.Minecraft;

/**
 * Represents transient screen-level state for an active drag operation.
 */
public interface IDragState {

    /**
     * Updates the drag representation for the current frame.
     *
     * @param minecraft the active client instance
     * @param mouseX current mouse x position in screen coordinates
     * @param mouseY current mouse y position in screen coordinates
     * @param screen the owning MKWidgets screen
     */
    void updateDragState(Minecraft minecraft, int mouseX, int mouseY, IMKScreen screen);
}
