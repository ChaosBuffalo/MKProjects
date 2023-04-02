package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;

public abstract class ScrollingList extends MKWidget {
    private final MKScrollView scrollView;
    private final MKStackLayoutVertical stackLayout;

    public ScrollingList(int x, int y, int width, int height) {
        super(x, y, width, height);
        scrollView = new MKScrollView(x, y,
                width, height, true);
        stackLayout = new MKStackLayoutVertical(0, 0, width);
        stackLayout.setMargins(5, 5, 5, 5);
        stackLayout.setPaddingBot(1);
        addWidget(scrollView);
        scrollView.addWidget(stackLayout);
    }

    public void populate(){
        stackLayout.clearWidgets();
        populateList(stackLayout);
        stackLayout.manualRecompute();
        scrollView.setToRight();
        scrollView.setToTop();
    }

    public MKScrollView getScrollView() {
        return scrollView;
    }

    public MKStackLayoutVertical getStackLayout() {
        return stackLayout;
    }

    protected abstract void populateList(MKStackLayoutVertical layout);
}
