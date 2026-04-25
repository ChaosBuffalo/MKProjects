package com.chaosbuffalo.mkwidgets.client.gui.actions;

import com.chaosbuffalo.mkwidgets.client.gui.instructions.DrawWidgetInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import net.minecraft.client.Minecraft;

/**
 * Drag state that renders a widget as a floating preview under the mouse cursor.
 */
public class WidgetHoldingDragState implements IDragState {
    private final IMKWidget widget;

    /**
     * Creates a drag state that renders the given widget while dragging.
     *
     * @param widget the widget to display as the drag preview
     */
    public WidgetHoldingDragState(IMKWidget widget) {
        this.widget = widget;
    }

    @Override
    public void updateDragState(Minecraft minecraft, int mouseX, int mouseY, IMKScreen screen) {
        screen.addPostRenderInstruction(new DrawWidgetInstruction(widget, new Vec2i(mouseX, mouseY), minecraft));
    }
}
