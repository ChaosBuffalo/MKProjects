package com.chaosbuffalo.mkwidgets.client.gui.layouts;


import com.chaosbuffalo.mkwidgets.client.gui.constraints.IConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;

public interface IMKLayout extends IMKWidget {

    IMKLayout setMarginTop(int value);

    int getMarginTop();

    default boolean addWidget(IMKWidget widget, IConstraint... constraints){
        boolean ret = addWidget(widget);
        if (ret){
            for (IConstraint constraint : constraints){
                addConstraintToWidget(constraint, widget);
            }
        }
        return ret;
    }

    default IMKLayout setMargins(int left, int right, int top, int bottom){
        return setMarginLeft(left).setMarginRight(right).setMarginTop(top).setMarginBot(bottom);
    }

    default void layoutWidget(IMKWidget widget, int index) {
    }

    void addConstraintToWidget(IConstraint constraint, IMKWidget widget);

    void clearWidgetConstraints(IMKWidget widget);

    void removeConstraintFromWidget(IConstraint constraint, IMKWidget widget);

    default void preLayout() {
    }

    default void postLayout() {

    }

    default IMKLayout setPaddings(int left, int right, int top, int bottom){
        return setPaddingLeft(left).setPaddingRight(right).setPaddingTop(top).setPaddingBot(bottom);
    }

    IMKLayout setMarginBot(int value);

    int getMarginBot();

    IMKLayout setMarginLeft(int value);

    int getMarginLeft();

    IMKLayout setMarginRight(int value);

    int getMarginRight();

    IMKLayout setPaddingTop(int value);

    int getPaddingTop();

    IMKLayout setPaddingBot(int value);

    int getPaddingBot();

    IMKLayout setPaddingLeft(int value);

    int getPaddingLeft();

    IMKLayout setPaddingRight(int value);

    int getPaddingRight();

}
