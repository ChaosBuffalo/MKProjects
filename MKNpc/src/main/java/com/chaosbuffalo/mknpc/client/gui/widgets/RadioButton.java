package com.chaosbuffalo.mknpc.client.gui.widgets;

import net.minecraft.client.gui.Font;

public class RadioButton<T> extends CenteringHorizontalLayout {
    private final HoverTextButton hoverTextButton;
    private final NestedRect nestedRect;
    private final RadioButtonList<T> list;
    private static final int SELECTED_COLOR = 0xff000000;
    private static final int UNSELECTED_COLOR = 0xffffffff;
    private final RadioButtonList.RadioValue<T> value;

    public RadioButton(int x, int y, int height, Font fontRenderer, RadioButtonList.RadioValue<T> value,
                       RadioButtonList<T> radioList) {
        super(x, y, height, fontRenderer);
        this.value = value;
        list = radioList;
        setPaddings(2, 2, 0, 0);
        nestedRect = new NestedRect(x, y, 9, 9, UNSELECTED_COLOR);
        addWidget(nestedRect);
        hoverTextButton = new HoverTextButton(fontRenderer, value.getName(), this::onSelected);
        hoverTextButton.setIsCentered(false);
        hoverTextButton.setWidth(Math.max(fontRenderer.width(value.getName()), 80));
        addWidget(hoverTextButton);

    }

    protected void onSelected(){
        nestedRect.setInnerColor(SELECTED_COLOR);
        list.notifySelected(this);
    }

    public RadioButtonList.RadioValue<T> getValue() {
        return value;
    }

    public String getOptionName() {
        return value.getName();
    }

    public void deselect(){
        nestedRect.setInnerColor(UNSELECTED_COLOR);
    }
}
