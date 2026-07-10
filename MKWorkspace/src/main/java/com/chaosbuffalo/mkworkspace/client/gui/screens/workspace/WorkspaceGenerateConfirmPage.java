package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
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
                "This change may rebuild workspace scaffold. Matching authored templates are preserved when possible; unmatched or affected pieces may be cleared and rebuilt."));
        MKText workspaceId = addHeaderText(screen, root, Component.literal(screen.draftSession().workspaceId()));

        int scrollTop = screen.scrollTopAfterHeader(root, workspaceId);
        int buttonRowsHeight = (screen.buttonHeight() * 2) + screen.buttonGap() + screen.bottomPadding();
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonRowsHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);
        addPreflightDetails(screen, content, screen.preflight());
        finishScrollContent(screen, scrollView, content);

        MKButton confirm = addBottomButton(screen, root, Component.literal("Apply Workspace Update"), 200, 1);
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

    private void addPreflightDetails(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                     MKWorkspaceMutationPreflight preflight) {
        if (preflight == null) {
            addText(screen, content, "Loading impact report...");
            return;
        }
        MKWorkspaceInvalidationReport report = preflight.report();
        addText(screen, content, report.summary());
        addText(screen, content, "Safety " + report.safety().getSerializedName() +
                " - operation " + report.recommendedOperation());
        if (!report.invalidatedLayers().isEmpty()) {
            addText(screen, content, "Invalidates: " + report.invalidatedLayers().stream()
                    .map(layer -> layer.getSerializedName())
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(""));
        }
        addRelayoutImpactReport(screen, content, report);
        if (report.relayoutImpacts().isEmpty()) {
            addText(screen, content, "No physical authored template relayout impacts were reported.");
        }
        if (!report.remapSuggestions().isEmpty()) {
            addText(screen, content, "Remap suggestions: " + report.remapSuggestions().size() +
                    " - accepted " + screen.draftSession().acceptedRemapCount());
        }
        for (String warning : report.warnings()) {
            addText(screen, content, "Warning: " + warning);
        }
    }
}
