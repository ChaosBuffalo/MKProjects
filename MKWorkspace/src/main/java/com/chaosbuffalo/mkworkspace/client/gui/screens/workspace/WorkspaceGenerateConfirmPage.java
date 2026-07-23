package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspaceGenerateConfirmPage extends WorkspacePageBase {
    public static final String ID = "generate_confirm";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Confirm Workspace Update"));
        addHeaderText(screen, root, Component.literal(
                "This will apply the draft settings using the safest available workspace update path."));
        MKText workspaceId = addHeaderText(screen, root, Component.literal(screen.draftSession().workspaceId()));

        int scrollTop = screen.scrollTopAfterHeader(root, workspaceId);
        int buttonRowsHeight = (screen.buttonHeight() * 2) + screen.buttonGap() + screen.bottomPadding();
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonRowsHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);
        if (screen.preflight() == null) {
            addText(screen, content, "Impact report is loading. The server will preflight again before applying.");
        } else {
            MKWorkspaceInvalidationReport report = screen.preflight().report();
            if (report.relayoutImpacts().isEmpty()) {
                addText(screen, content, "These changes will not add, rebuild, remove, move, expand, or patch " +
                        "physical authored pieces.");
            } else {
                addRelayoutImpactReport(screen, content, report);
            }

            for (String warning : report.warnings()) {
                addText(screen, content, "Warning: " + warning);
            }
            addText(screen, content, "Summary: " + report.summary());
            addText(screen, content, "Operation: " + report.recommendedOperation());
            addText(screen, content, "Safety: " + report.safety().getSerializedName());
            if (!report.invalidatedLayers().isEmpty()) {
                addText(screen, content, "Invalidates: " + report.invalidatedLayers().stream()
                        .map(layer -> layer.getSerializedName())
                        .reduce((left, right) -> left + ", " + right)
                        .orElse(""));
            }
        }
        finishScrollContent(screen, scrollView, content);

        MKButton confirm = addBottomButton(screen, root, Component.literal("Apply Workspace Update"), 220, 1);
        confirm.setPressedCallback((button, mouseButton) -> {
            button.setEnabled(false);
            button.buttonText = Component.literal("Applying...");
            screen.draftSession().send();
            return true;
        });

        MKButton cancel = addBottomButton(screen, root, Component.literal("Cancel"), 120, 0);
        cancel.setPressedCallback((button, mouseButton) -> {
            screen.switchToExistingState("form");
            return true;
        });
        return root;
    }
}
