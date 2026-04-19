package com.chaosbuffalo.mkwidgets.client.gui.instructions;

import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Post-render instruction that draws one or more tooltip lines at a given screen position.
 */
public class HoveringTextInstruction implements IInstruction {

    private final List<Component> texts;
    private final Vec2i mousePos;

    /**
     * Creates a single-line tooltip from a plain string.
     *
     * @param text tooltip text
     * @param mousePos tooltip anchor position
     */
    public HoveringTextInstruction(String text, Vec2i mousePos) {
        this(Component.literal(text), mousePos);
    }

    public HoveringTextInstruction(String text, int x, int y) {
        this(text, new Vec2i(x, y));
    }

    public HoveringTextInstruction(Component text, int x, int y) {
        this(text, new Vec2i(x, y));
    }

    public HoveringTextInstruction(Component text, Vec2i mousePos) {
        this(new ArrayList<>(), mousePos);
        texts.add(text);
    }

    public HoveringTextInstruction(List<Component> texts, int x, int y) {
        this(texts, new Vec2i(x, y));
    }

    public HoveringTextInstruction(List<Component> texts, Vec2i mousePos) {
        this.texts = new ArrayList<>(texts);
        this.mousePos = mousePos;
    }

    /**
     * Converts plain strings into tooltip components.
     *
     * @param texts tooltip lines
     * @param mousePos tooltip anchor position
     * @return a new tooltip instruction
     */
    public static HoveringTextInstruction fromStrings(List<String> texts, Vec2i mousePos) {
        return new HoveringTextInstruction(texts.stream().map(Component::literal).collect(Collectors.toList()), mousePos);
    }

    public static HoveringTextInstruction fromStrings(List<String> texts, int x, int y) {
        return fromStrings(texts, new Vec2i(x, y));
    }

    @Override
    public void draw(GuiGraphics graphics, Font renderer, int screenWidth, int screenHeight, float partialTicks, MKScreen screen) {
        graphics.renderTooltip(renderer, texts, Optional.empty(), mousePos.x, mousePos.y);
    }
}
