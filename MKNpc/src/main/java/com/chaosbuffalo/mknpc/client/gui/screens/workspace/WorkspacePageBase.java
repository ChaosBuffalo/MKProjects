package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public abstract class WorkspacePageBase implements WorkspacePage {

    protected MKLayout createPanel(WorkspacePageContext context) {
        MKLayout root = new MKLayout(context.panelX(), context.panelY(), context.panelWidth(), context.panelHeight());
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);
        return root;
    }

    protected MKText addTitle(WorkspacePageContext context, MKLayout root, Component text) {
        MKText title = context.makeWhiteText(text);
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);
        return title;
    }

    protected MKText addHeaderText(WorkspacePageContext context, MKLayout root, Component text) {
        MKText header = context.makeWhiteText(text);
        header.setWidth(context.contentWidth());
        header.setMultiline(true);
        root.addWidget(header);
        root.addConstraintToWidget(StackConstraint.VERTICAL, header);
        root.addConstraintToWidget(new CenterXConstraint(), header);
        return header;
    }

    protected MKScrollView addScrollBelowHeader(WorkspacePageContext context, MKLayout root, MKText headerText) {
        int scrollTop = context.scrollTopAfterHeader(root, headerText);
        int scrollHeight = context.panelY() + context.panelHeight() - context.bottomPadding() -
                context.buttonHeight() - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(context.panelX() + 10, scrollTop,
                context.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);
        return scrollView;
    }

    protected MKStackLayoutVertical createContentStack(WorkspacePageContext context) {
        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, context.contentWidth());
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);
        return content;
    }

    protected void finishScrollContent(WorkspacePageContext context, MKScrollView scrollView,
                                       MKStackLayoutVertical content) {
        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        context.finalizeScrollView(scrollView, id());
    }

    protected MKButton addBottomButton(WorkspacePageContext context, MKLayout root, Component label,
                                       int buttonWidth, int rowsAboveBottom) {
        MKButton button = new MKButton(label, buttonWidth, context.buttonHeight());
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
        int rowOffset = rowsAboveBottom * (context.buttonHeight() + context.buttonGap());
        button.setY(context.panelY() + context.panelHeight() - context.bottomPadding() -
                context.buttonHeight() - rowOffset);
        return button;
    }

    protected MKButton addBackButton(WorkspacePageContext context, MKLayout root, String targetState) {
        MKButton back = addBottomButton(context, root, Component.literal("Back"), 120, 0);
        back.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState(targetState);
            return true;
        });
        return back;
    }
}
