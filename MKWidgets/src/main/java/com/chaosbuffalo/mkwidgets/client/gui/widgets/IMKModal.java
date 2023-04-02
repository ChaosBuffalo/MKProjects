package com.chaosbuffalo.mkwidgets.client.gui.widgets;

public interface IMKModal extends IMKWidget {

    IMKModal setCloseOnClickOutside(boolean value);

    boolean shouldCloseOnClickOutside();

    IMKModal setOnCloseCallback(Runnable callback);

    Runnable getOnCloseCallback();

}
