package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class RadioButtonList<T> extends MKStackLayoutVertical {
    private final Font fontRenderer;
    private final List<RadioButton<T>> buttons;
    private final Map<T, RadioButton<T>> buttonIndex;
    private final Consumer<T> selectCallback;

    public static class RadioValue<T> {
        private final T value;
        private final String name;

        public RadioValue(T value, String name){
            this.value = value;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public T getValue() {
            return value;
        }
    }

    public RadioButtonList(int x, int y, int width, Font fontRenderer, String name,
                           List<RadioValue<T>> values, Consumer<T> selectCallback) {
        super(x, y, width);
        this.fontRenderer = fontRenderer;
        this.selectCallback = selectCallback;
        MKText nameText = new MKText(fontRenderer, name);
        addWidget(nameText);
        MKRectangle divider = new MKRectangle(0, 0, width, 1, 0xaaffffff);
        addWidget(divider);
        buttons = new ArrayList<>();
        setPaddingBot(2);
        setPaddingTop(2);
        buttonIndex = new HashMap<>();
        for (RadioValue<T> value : values){
            RadioButton<T> button = new RadioButton<T>(x, y, 9, fontRenderer, value, this);
            buttons.add(button);
            buttonIndex.put(value.getValue(), button);
            addWidget(button);
        }
    }


    public void selectEntry(T entryKey){
        if (buttonIndex.containsKey(entryKey)){
            buttonIndex.get(entryKey).onSelected();
        }
    }

    public void notifySelected(RadioButton<T> selected){
        selectCallback.accept(selected.getValue().getValue());
        for (RadioButton<T> button : buttons){
            if (button.equals(selected)){
                continue;
            }
            button.deselect();
        }
    }
}
