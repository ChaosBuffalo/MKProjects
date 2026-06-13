package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public abstract class WorkspacePageBase {

    public abstract String id();

    public abstract MKLayout build(MKWorkspaceScreen screen);

    protected MKLayout createPanel(MKWorkspaceScreen screen) {
        MKLayout root = new MKLayout(screen.panelX(), screen.panelY(), screen.panelWidth(), screen.panelHeight());
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);
        return root;
    }

    protected MKText addTitle(MKWorkspaceScreen screen, MKLayout root, Component text) {
        MKText title = screen.makeWhiteText(text);
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);
        return title;
    }

    protected MKText addHeaderText(MKWorkspaceScreen screen, MKLayout root, Component text) {
        MKText header = screen.makeWhiteText(text);
        header.setWidth(screen.contentWidth());
        header.setMultiline(true);
        root.addWidget(header);
        root.addConstraintToWidget(StackConstraint.VERTICAL, header);
        root.addConstraintToWidget(new CenterXConstraint(), header);
        return header;
    }

    protected MKScrollView addScrollBelowHeader(MKWorkspaceScreen screen, MKLayout root, MKText headerText) {
        int scrollTop = screen.scrollTopAfterHeader(root, headerText);
        int scrollHeight = screen.panelY() + screen.panelHeight() - screen.bottomPadding() -
                screen.buttonHeight() - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);
        return scrollView;
    }

    protected MKStackLayoutVertical createContentStack(MKWorkspaceScreen screen) {
        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, screen.contentWidth());
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);
        return content;
    }

    protected void finishScrollContent(MKWorkspaceScreen screen, MKScrollView scrollView,
                                       MKStackLayoutVertical content) {
        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        screen.finalizeScrollView(scrollView, id());
    }

    protected MKButton addBottomButton(MKWorkspaceScreen screen, MKLayout root, Component label,
                                       int buttonWidth, int rowsAboveBottom) {
        MKButton button = new MKButton(label, buttonWidth, screen.buttonHeight());
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
        int rowOffset = rowsAboveBottom * (screen.buttonHeight() + screen.buttonGap());
        button.setY(screen.panelY() + screen.panelHeight() - screen.bottomPadding() -
                screen.buttonHeight() - rowOffset);
        return button;
    }

    protected MKButton addBackButton(MKWorkspaceScreen screen, MKLayout root, String targetState) {
        MKButton back = addBottomButton(screen, root, Component.literal("Back"), 120, 0);
        back.setPressedCallback((button, mouseButton) -> {
            screen.goBackOrSwitchTo(targetState);
            return true;
        });
        return back;
    }
}
