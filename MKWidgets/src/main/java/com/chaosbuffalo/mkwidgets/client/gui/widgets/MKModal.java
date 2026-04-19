package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Screen-wide modal overlay widget.
 * <p>
 * A modal usually fills the screen, optionally draws a dim background, and receives input before ordinary
 * root widgets.
 */
public class MKModal extends MKWidget implements IMKModal {

    private boolean doBackground;
    private int backgroundColor;
    private boolean closeOnClickOutsideContent;
    private Runnable onCloseCallback;

    /**
     * Creates a modal with a translucent dark background and outside-click-to-close enabled.
     */
    public MKModal() {
        super(0, 0, 200, 20);
        doBackground = true;
        backgroundColor = 0x7D000000;
        closeOnClickOutsideContent = true;
    }

    /**
     * Sets the color drawn behind modal content when the modal background is enabled.
     *
     * @param color background fill color
     * @return this modal
     */
    public MKModal setBackgroundColor(int color) {
        backgroundColor = color;
        return this;
    }

    @Override
    public MKModal setCloseOnClickOutside(boolean value) {
        closeOnClickOutsideContent = value;
        return this;
    }

    @Override
    public boolean shouldCloseOnClickOutside() {
        return closeOnClickOutsideContent;
    }

    @Override
    public MKModal setOnCloseCallback(Runnable callback) {
        onCloseCallback = callback;
        return this;
    }

    @Override
    public Runnable getOnCloseCallback() {
        return onCloseCallback;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (getScreen() != null && shouldCloseOnClickOutside()) {
            IMKScreen screen = getScreen();
            screen.closeModal(this);
            return true;
        }
        return super.onMousePressed(minecraft, mouseX, mouseY, mouseButton);
    }


    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Enables or disables the full-screen modal background fill.
     *
     * @param value {@code true} to draw the modal background
     * @return this modal
     */
    public MKModal setDoBackground(boolean value) {
        doBackground = value;
        return this;
    }

    public boolean shouldDoBackground() {
        return doBackground;
    }

    @Override
    public void preDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        super.preDraw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
        // We need to clear the depth buffer so that text from previous draws doesnt render on top of our modals.
        // FIXME: In 1.21.5 we need to change this to be RenderSystem.getDevice().createCommandEncoder().clearDepthTexture
        RenderSystem.clear(256, Minecraft.ON_OSX);
        if (shouldDoBackground()) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), getBackgroundColor());
        }
    }
}
