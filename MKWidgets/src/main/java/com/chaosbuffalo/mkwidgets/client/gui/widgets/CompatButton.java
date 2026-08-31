package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.BiFunction;

/**
 * Compact, list-style button with the activation behavior of {@link MKButton}.
 *
 * <p>Unlike the sprite-backed standard button, this control renders as a left-aligned text row and communicates
 * hover and selection with translucent fills. It is intended for dense sidebars, pickers, and selectable lists where
 * the standard 20-pixel button chrome consumes too much space.</p>
 */
public class CompatButton extends MKButton {
    public static final int DEFAULT_COMPACT_HEIGHT = 14;
    public static final int HORIZONTAL_TEXT_PADDING = 4;
    public static final int HOVER_BACKGROUND = 0x55ffffff;
    public static final int SELECTED_BACKGROUND = 0x99ffffff;

    private boolean selected;

    public CompatButton(String buttonText, int width) {
        this(Component.literal(buttonText), width);
    }

    public CompatButton(Component buttonText, int width) {
        this(0, 0, width, DEFAULT_COMPACT_HEIGHT, buttonText);
    }

    public CompatButton(String buttonText, int width, int height) {
        this(Component.literal(buttonText), width, height);
    }

    public CompatButton(Component buttonText, int width, int height) {
        this(0, 0, width, height, buttonText);
    }

    public CompatButton(int x, int y, int width, int height, Component buttonText) {
        super(x, y, width, height, buttonText);
    }

    public boolean isSelected() {
        return selected;
    }

    public CompatButton setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    @Override
    public CompatButton setPressedCallback(BiFunction<MKButton, Integer, Boolean> callback) {
        super.setPressedCallback(callback);
        return this;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                     int mouseX, int mouseY, float partialTicks) {
        boolean highlighted = isEnabled() &&
                (isHovered() || (getScreen() != null && this.equals(getScreen().getFocus())));
        if (highlighted) {
            graphics.fill(x, y, x + width, y + height, HOVER_BACKGROUND);
        }
        if (selected) {
            graphics.fill(x, y, x + width, y + height, SELECTED_BACKGROUND);
        }

        Font font = mc.font;
        int color = !isEnabled() ? 0xffa0a0a0 : highlighted ? 0xffffff00 : 0xffffffff;
        graphics.drawString(font, buttonText, x + HORIZONTAL_TEXT_PADDING,
                y + (height - font.lineHeight) / 2, color, false);
    }
}
