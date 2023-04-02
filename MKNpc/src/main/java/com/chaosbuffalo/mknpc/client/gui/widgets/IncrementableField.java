package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.BiConsumer;

public class IncrementableField extends CenteringHorizontalLayout {
    private final MKText text;
    private double value;
    private final String name;

    public IncrementableField(int x, int y, int height, String name, double value, Font fontRenderer,
                              BiConsumer<IncrementableField, Double> callback) {
        super(x, y, height, fontRenderer);
        this.name = name;
        this.value = value;
        String textStr = String.format("%s: %.2f", name, value);
        text = new MKText(fontRenderer, textStr);
        text.setWidth(fontRenderer.width(textStr));
        setPaddingLeft(2);
        setPaddingRight(2);
        addWidget(text);
        HoverTextButton plusButton = new HoverTextButton(fontRenderer, "+", () -> {
            if (Screen.hasShiftDown()){
                callback.accept(this, getValue() + 10.0);
                updateText();
            } else {
                callback.accept(this, getValue() + 1.0);
                updateText();
            }
        });
        addWidget(plusButton);
        HoverTextButton minusButton = new HoverTextButton(fontRenderer, "-", () -> {
            if (Screen.hasShiftDown()){
                callback.accept(this, getValue() - 10.0);
                updateText();
            } else {
                callback.accept(this, getValue() - 1.0);
                updateText();
            }
        });
        addWidget(minusButton);
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void updateText(){
        String textStr = String.format("%s: %.2f", getName(), getValue());
        text.setText(textStr);
    }
}
