package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

public class SerializableAttributeEntry extends MKStackLayoutHorizontal {
    private final ISerializableAttribute<?> attr;
    private Consumer<ISerializableAttribute<?>> callback;

    public SerializableAttributeEntry(int x, int y, ISerializableAttribute<?> attr, Font renderer) {
        super(x, y, renderer.lineHeight + 4);
        this.attr = attr;
        setMargins(3, 2, 2, 2);
        setPaddings(20, 2, 2, 2);
        MKText nameText = new MKText(renderer, attr.getName(), 100);
        nameText.setColor(0xffffffff);
        MKTextFieldWidget textField = new MKTextFieldWidget(renderer, x, y, 50, renderer.lineHeight + 2,
                new TextComponent(attr.getName()));
        textField.setText(attr.valueAsString());
        textField.getContainedWidget().moveCursorToStart();
        textField.setSubmitCallback((wid, str) -> {
            if (!attr.isEmptyStringInput(str)) {
                attr.setValueFromString(str);
            } else {
                attr.reset();
                textField.setText(attr.valueAsString());
            }
            if (callback != null) {
                callback.accept(attr);
            }
        });
        textField.setTextValidator(attr::validateString);
        addWidget(nameText);
        addWidget(textField);
    }

    public void setCallback(Consumer<ISerializableAttribute<?>> callback) {
        this.callback = callback;
    }

    public ISerializableAttribute<?> getAttr() {
        return attr;
    }
}
