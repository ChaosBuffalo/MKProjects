package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;

public class NamedField extends MKStackLayoutHorizontal {

    public NamedField(int x, int y, String name, int nameColor,
                      String value, int valueColor, Font renderer) {
        super(x, y, renderer.lineHeight + 2);
        setMargins(1, 1, 1, 1);
        setPaddings(1, 1, 0, 0);
        MKText nameText = new MKText(renderer, name, renderer.width(name));
        nameText.setColor(nameColor);
        MKText valueText = new MKText(renderer, value, renderer.width(value));
        valueText.setColor(valueColor);
        addWidget(nameText);
        addWidget(valueText);
    }
}
