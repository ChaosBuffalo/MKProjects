package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;

public class NestedRect extends MKRectangle {

    private final MKRectangle innerRectangle;

    public NestedRect(int x, int y, int width, int height, int color) {
        super(x, y, width, height, color);
        innerRectangle = new MKRectangle(x + 1, y + 1, width - 2, height - 2, color);
        addWidget(innerRectangle);
    }

    @Override
    public IMKWidget setX(int newX) {
        innerRectangle.setX(newX + 1);
        return super.setX(newX);
    }

    @Override
    public IMKWidget setY(int newY) {
        innerRectangle.setY(newY + 1);
        return super.setY(newY);
    }

    @Override
    public IMKWidget setWidth(int newWidth) {
        innerRectangle.setWidth(newWidth - 2);
        return super.setWidth(newWidth);
    }

    @Override
    public IMKWidget setHeight(int newHeight) {
        innerRectangle.setHeight(newHeight - 2);
        return super.setHeight(newHeight);
    }

    public void setInnerColor(int innerColor){
        innerRectangle.setColor(innerColor);
    }
}
