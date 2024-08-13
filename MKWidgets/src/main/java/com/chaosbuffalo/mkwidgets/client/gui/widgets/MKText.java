package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.common.util.Lazy;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class MKText extends MKWidget {

    public Supplier<Component> text;
    private final Font fontRenderer;
    public int color;
    public boolean isMultiline;
    public boolean isCentered;

    public MKText(Font renderer, Supplier<Component> text, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.color = 0;
        this.fontRenderer = renderer;
        this.text = text;
        this.isMultiline = false;
    }

    public MKText(Font renderer, Component text) {
        this(renderer, text, 200, renderer.lineHeight);
    }

    public MKText(Font renderer, Supplier<Component> text) {
        this(renderer, text, 0, 0, 200, renderer.lineHeight);
    }

    public MKText(Font renderer, Component text, int x, int y) {
        this(renderer, () -> text, x, y, 200, renderer.lineHeight);
    }

    public MKText(Font renderer, String text) {
        this(renderer, text, 200);
    }

    public MKText(Font renderer, String text, int x, int y) {
        this(renderer, text, x, y, 200, renderer.lineHeight);
    }

    public MKText(Font renderer, String text, int x, int y, int width, int height) {
        this(renderer, Lazy.of(() -> Component.literal(text)), x, y, width, height);
    }

    public MKText(Font renderer, String text, int width) {
        this(renderer, text, 0, 0, width, renderer.lineHeight);
    }

    public int getFontHeight() {
        return fontRenderer.lineHeight;
    }

    public int getColor() {
        return color;
    }

    public MKText setColor(int i) {
        this.color = i;
        return this;
    }

    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        Component formattedText = getText();
        if (isCentered()) {
            this.drawCenteredStringNoDropShadow(graphics, this.fontRenderer,
                    formattedText,
                    this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - this.fontRenderer.lineHeight) / 2, color);
        } else if (isMultiline()) {
            drawStringMultiline(fontRenderer, graphics, formattedText, getX(), getY(), getWidth(), color);
        } else {
            drawString(fontRenderer, graphics, formattedText, getX(), getY(), color);
        }
    }

    protected void drawString(Font font, GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, false);
    }

    protected void drawStringShadow(Font font, GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    private int drawInternalDuplicate(GuiGraphics graphics, Font font, FormattedCharSequence seq, float x, float y, int color, Matrix4f mat, boolean shadow) {
        MultiBufferSource.BufferSource buffer = graphics.bufferSource();
        int i = font.drawInBatch(seq, x, y, color, shadow, mat, buffer, Font.DisplayMode.NORMAL, 0, 15728880);
        buffer.endBatch();
        return i;
    }

    private void drawWordWrap(GuiGraphics graphics, Font font, FormattedText text, int x, int y, int width, int color) {
        for (FormattedCharSequence formattedcharsequence : font.split(text, width)) {
            drawInternalDuplicate(graphics, font, formattedcharsequence, (float) x, (float) y, color, graphics.pose().last().pose(), false);
            y += 9;
        }
    }

    protected void drawStringMultiline(Font font, GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        drawWordWrap(graphics, font, text, x, y, width, color);
    }

    public void drawCenteredStringNoDropShadow(GuiGraphics graphics, Font fontRenderer, String string, int x, int y, int color) {
        graphics.drawString(fontRenderer, string, (x - fontRenderer.width(string) / 2), y, color, false);
    }

    public void drawCenteredStringNoDropShadow(GuiGraphics graphics, Font fontRenderer, Component string, int x, int y, int color) {
        drawString(fontRenderer, graphics, string, (x - fontRenderer.width(string) / 2), y, color);
    }

    public MKText setIsCentered(boolean isCentered) {
        this.isCentered = isCentered;
        return this;
    }

    public boolean isCentered() {
        return isCentered;
    }

    public MKText setText(String text) {
        return setText(Lazy.of(() -> Component.literal(text)));
    }

    public MKText setText(Component text) {
        return setText(() -> text);
    }

    public MKText setText(Supplier<Component> text) {
        this.text = text;
        updateLabel();
        return this;
    }


    public Component getText() {
        return text.get();
    }

    public MKText setMultiline(boolean multiline) {
        this.isMultiline = multiline;
        updateLabel();
        return this;
    }

    public boolean isMultiline() {
        return isMultiline;
    }

    @Override
    public IMKWidget setWidth(int newWidth) {
        super.setWidth(newWidth);
        updateLabel();
        return this;
    }


    private void updateLabel() {
        if (isMultiline()) {
            setHeight(fontRenderer.wordWrapHeight(getText().getString(), getWidth()));
        } else {
            setHeight(fontRenderer.lineHeight);
        }
    }
}
