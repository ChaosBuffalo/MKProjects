package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.serialization.attributes.IGuiDisplayAttribute;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class SerializableAttributeEntry extends MKStackLayoutHorizontal {
    private Consumer<IGuiDisplayAttribute> callback;

    public SerializableAttributeEntry(int x, int y, String name, IGuiDisplayAttribute attr, Font renderer) {
        super(x, y, renderer.lineHeight + 4);
        setMargins(3, 2, 2, 2);
        setPaddings(20, 2, 2, 2);
        MKText nameText = new MKText(renderer, name, 100);
        nameText.setColor(0xffffffff);
        MKTextFieldWidget textField = new MKTextFieldWidget(renderer, x, y, 50, renderer.lineHeight + 2,
                Component.translatable(name));
        textField.setText(attr.valueAsString());
        textField.getContainedWidget().moveCursorToStart(false);
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

    public void setCallback(Consumer<IGuiDisplayAttribute> callback) {
        this.callback = callback;
    }
}
