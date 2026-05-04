package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import net.minecraft.client.Minecraft;

public class MKBlockingModal extends MKModal {
    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        return true;
    }

    @Override
    public boolean onMouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double pScrollX,
                                      double pScrollY) {
        return true;
    }

    @Override
    public boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton,
                                  double dX, double dY) {
        return true;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        return true;
    }
}
