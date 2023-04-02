package com.chaosbuffalo.mkwidgets.client.gui.actions;

import com.chaosbuffalo.mkwidgets.client.gui.instructions.DrawWidgetInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import net.minecraft.client.Minecraft;

public class WidgetHoldingDragState implements IDragState {
    private final IMKWidget widget;

    public WidgetHoldingDragState(IMKWidget widget){
        this.widget = widget;
    }

    @Override
    public void updateDragState(Minecraft minecraft, int mouseX, int mouseY, IMKScreen screen) {
        screen.addPostRenderInstruction(new DrawWidgetInstruction(widget, new Vec2i(mouseX, mouseY), minecraft));
    }
}
