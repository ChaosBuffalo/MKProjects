package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CycleButton<T> extends MKButton {
    private final List<T> elements;
    private final Function<T, Component> describer;
    private final Consumer<T> onValueChanged;
    private int index;

    public CycleButton(List<T> elements, Function<T, Component> describer, Consumer<T> onValueChanged) {
        super("");
        Preconditions.checkState(elements.size() > 0, "Cycle button must have >0 elements");
        index = 0;
        this.elements = elements;
        this.describer = describer;
        this.onValueChanged = onValueChanged;
        updateText();
        setPressedCallback(this::buttonPressed);
    }

    public boolean buttonPressed(MKButton button, int mouseButton) {
        int prevIndex = index;
        index = (index + 1) % elements.size();
        if (index != prevIndex) {
            updateText();
            onValueChanged.accept(current());
        }
        return true;
    }

    void updateText() {
        T current = current();
        buttonText = describer.apply(current);
    }

    public void setCurrent(T value) {
        index = elements.indexOf(value);
        if (index == -1)
            index = 0;
        updateText();
    }

    public T current() {
        return elements.get(index);
    }
}
