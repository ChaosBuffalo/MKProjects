package com.chaosbuffalo.mkwidgets.client.gui.instructions;

import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HoveringTextInstruction implements IInstruction {

    private final List<Component> texts;
    private final Vec2i mousePos;

    public HoveringTextInstruction(String text, Vec2i mousePos) {
        this(new TextComponent(text), mousePos);
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

    public static HoveringTextInstruction fromStrings(List<String> texts, Vec2i mousePos) {
        return new HoveringTextInstruction(texts.stream().map(TextComponent::new).collect(Collectors.toList()), mousePos);
    }

    public static HoveringTextInstruction fromStrings(List<String> texts, int x, int y) {
        return fromStrings(texts, new Vec2i(x, y));
    }

    @Override
    public void draw(PoseStack matrixStack, Font renderer, int screenWidth, int screenHeight, float partialTicks, MKScreen screen) {
        screen.renderTooltip(matrixStack, texts, Optional.empty(), mousePos.x, mousePos.y, renderer);
    }
}
