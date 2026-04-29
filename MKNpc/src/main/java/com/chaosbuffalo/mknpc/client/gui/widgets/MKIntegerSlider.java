package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;

public class MKIntegerSlider extends MKWidget {
    private final String label;
    private final int minValue;
    private final int maxValue;
    private final int step;
    private final List<Integer> allowedValues;
    private final IntConsumer callback;
    private int value;
    private boolean dragging;

    public MKIntegerSlider(String label, int width, int height, int minValue, int maxValue, int value,
                           IntConsumer callback) {
        this(label, width, height, minValue, maxValue, 1, value, callback);
    }

    public MKIntegerSlider(String label, int width, int height, int minValue, int maxValue, int step, int value,
                           IntConsumer callback) {
        super(0, 0, width, height);
        this.label = label;
        this.minValue = minValue;
        this.maxValue = Math.max(minValue, maxValue);
        this.step = Math.max(1, step);
        this.allowedValues = List.of();
        this.value = clamp(value);
        this.callback = callback;
        setCanFocus(true);
        setTooltip(Component.literal(label + " " + this.minValue + ".." + this.maxValue));
    }

    public MKIntegerSlider(String label, int width, int height, List<Integer> allowedValues, int value,
                           IntConsumer callback) {
        super(0, 0, width, height);
        this.label = label;
        this.allowedValues = normalizeAllowedValues(allowedValues, value);
        this.minValue = this.allowedValues.getFirst();
        this.maxValue = this.allowedValues.getLast();
        this.step = 1;
        this.value = clamp(value);
        this.callback = callback;
        setCanFocus(true);
        setTooltip(Component.literal(label + " " + this.minValue + ".." + this.maxValue));
    }

    public int value() {
        return value;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        dragging = true;
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton,
                                  double dX, double dY) {
        if (!dragging) {
            return false;
        }
        updateFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        dragging = false;
        return true;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY,
                     float partialTicks) {
        int trackY = getY() + (getHeight() / 2) - 2;
        int trackLeft = getX() + 4;
        int trackRight = getX() + getWidth() - 4;
        graphics.fill(trackLeft, trackY, trackRight, trackY + 4, isEnabled() ? 0xff555555 : 0xff333333);
        int knobX = valueToX(trackLeft, trackRight);
        graphics.fill(knobX - 3, getY() + 2, knobX + 4, getY() + getHeight() - 2,
                isHovered() || dragging ? 0xffffffff : 0xffcccccc);
        graphics.drawCenteredString(mc.font, label + ": " + value,
                getX() + (getWidth() / 2), getY() + (getHeight() - 8) / 2, isEnabled() ? 0xffffff : 0x808080);
    }

    private void updateFromMouse(double mouseX) {
        int nextValue = valueFromX(mouseX, getX() + 4, getX() + getWidth() - 4);
        if (nextValue != value) {
            value = nextValue;
            callback.accept(value);
        }
    }

    private int valueFromX(double mouseX, int trackLeft, int trackRight) {
        if (maxValue == minValue || trackRight <= trackLeft) {
            return minValue;
        }
        if (!allowedValues.isEmpty()) {
            double normalized = Math.max(0.0, Math.min(1.0, (mouseX - trackLeft) / (trackRight - trackLeft)));
            int index = (int) Math.round(normalized * (allowedValues.size() - 1));
            return allowedValues.get(Math.max(0, Math.min(allowedValues.size() - 1, index)));
        }
        double normalized = Math.max(0.0, Math.min(1.0, (mouseX - trackLeft) / (trackRight - trackLeft)));
        int steps = (maxValue - minValue) / step;
        return clamp(minValue + ((int) Math.round(normalized * steps) * step));
    }

    private int valueToX(int trackLeft, int trackRight) {
        if (maxValue == minValue || trackRight <= trackLeft) {
            return (trackLeft + trackRight) / 2;
        }
        if (!allowedValues.isEmpty()) {
            int index = allowedValues.indexOf(value);
            if (index < 0) {
                index = 0;
            }
            double normalized = (double) index / (double) Math.max(1, allowedValues.size() - 1);
            return trackLeft + (int) Math.round(normalized * (trackRight - trackLeft));
        }
        int steps = Math.max(1, (maxValue - minValue) / step);
        double normalized = (double) ((value - minValue) / step) / (double) steps;
        return trackLeft + (int) Math.round(normalized * (trackRight - trackLeft));
    }

    private int clamp(int candidate) {
        if (!allowedValues.isEmpty()) {
            return allowedValues.stream()
                    .min(Comparator.comparingInt(value -> Math.abs(value - candidate)))
                    .orElse(candidate);
        }
        int bounded = Math.max(minValue, Math.min(maxValue, candidate));
        int stepped = minValue + (Math.round((float) (bounded - minValue) / (float) step) * step);
        return Math.max(minValue, Math.min(maxValue, stepped));
    }

    private static List<Integer> normalizeAllowedValues(List<Integer> values, int fallbackValue) {
        List<Integer> normalized = new ArrayList<>(values == null ? List.of() : values);
        normalized.removeIf(value -> value == null);
        normalized.sort(Integer::compareTo);
        List<Integer> distinct = normalized.stream().distinct().toList();
        if (distinct.isEmpty()) {
            return List.of(fallbackValue);
        }
        return distinct;
    }
}
