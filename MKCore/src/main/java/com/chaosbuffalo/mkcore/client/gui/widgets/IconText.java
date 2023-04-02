package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IconText extends MKStackLayoutHorizontal {

    protected MKText text;
    protected MKImage icon;

    public IconText(int x, int y, int height, Component textStr, ResourceLocation iconLoc,
                    Font font, int iconWidth, int offset) {
        super(x, y, height);
        setPaddingRight(2);
        setPaddingLeft(2);
        icon = new MKImage(0, 0, iconWidth, height, iconLoc);
        addWidget(icon);
        text = new MKText(font, textStr);
        text.setWidth(font.width(textStr));
        text.setColor(0xffffffff);
        addWidget(text);
        addConstraintToWidget(new CenterYWithOffsetConstraint(offset), text);
    }

    public MKText getText() {
        return text;
    }

}
