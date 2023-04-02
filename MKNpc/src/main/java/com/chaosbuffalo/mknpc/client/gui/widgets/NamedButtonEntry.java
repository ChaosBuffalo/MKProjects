package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;

public class NamedButtonEntry extends CenteringHorizontalLayout {
    private final MKButton button;
    private final MKText label;


    public NamedButtonEntry(int x, int y, int height, String buttonText,
                            String nameText, Font fontRenderer) {
        super(x, y, height, fontRenderer);
        button = new MKButton(Math.max(fontRenderer.width(buttonText), 100),
                buttonText);
        label = new MKText(fontRenderer, nameText, fontRenderer.width(nameText));
        addWidget(label);
        addWidget(button);
    }

    public void updateButtonText(String newText){
        button.setWidth(Math.max(fontRenderer.width(newText), 100));
        button.buttonText = new TextComponent(newText);
    }

    public MKButton getButton() {
        return button;
    }
}
