package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

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
        int buttonRowsHeight = (screen.buttonHeight() * 3) + (screen.buttonGap() * 2) + screen.bottomPadding();
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonRowsHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);
        List<String> summaryLines = preflightSummaryLines(screen);
        for (String line : summaryLines) {
            addText(screen, content, line);
        }
        finishScrollContent(screen, scrollView, content);

        MKButton confirm = addBottomButton(screen, root, Component.literal("Apply Workspace Update"), 220, 1);
        confirm.setPressedCallback((button, mouseButton) -> {
            button.setEnabled(false);
            button.buttonText = Component.literal("Applying...");
            screen.draftSession().send();
            return true;
        });

        MKButton copy = addBottomButton(screen, root, Component.literal("Copy Summary"), 140, 2);
        copy.setPressedCallback((button, mouseButton) -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(preflightClipboardText(screen, summaryLines));
            button.buttonText = Component.literal("Copied Summary");
            return true;
        });

        MKButton cancel = addBottomButton(screen, root, Component.literal("Cancel"), 120, 0);
        cancel.setPressedCallback((button, mouseButton) -> {
            screen.switchToExistingState("form");
            return true;
        });
        return root;
    }

    private List<String> preflightSummaryLines(MKWorkspaceScreen screen) {
        ArrayList<String> lines = new ArrayList<>();
        if (screen.preflight() == null) {
            lines.add("Impact report is loading. The server will preflight again before applying.");
            return List.copyOf(lines);
        }

        MKWorkspaceInvalidationReport report = screen.preflight().report();
        if (report.relayoutImpacts().isEmpty()) {
            lines.add("These changes will not add, rebuild, remove, move, expand, or patch physical authored pieces.");
        } else {
            lines.addAll(relayoutImpactReportLines(report));
        }

        for (String warning : report.warnings()) {
            lines.add("Warning: " + warning);
        }
        lines.add("Summary: " + report.summary());
        lines.add("Operation: " + report.recommendedOperation());
        lines.add("Safety: " + report.safety().getSerializedName());
        if (!report.invalidatedLayers().isEmpty()) {
            lines.add("Invalidates: " + report.invalidatedLayers().stream()
                    .map(layer -> layer.getSerializedName())
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(""));
        }
        return List.copyOf(lines);
    }

    private String preflightClipboardText(MKWorkspaceScreen screen, List<String> summaryLines) {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Confirm Workspace Update");
        lines.add(screen.draftSession().workspaceId());
        lines.add("");
        lines.addAll(summaryLines);
        return String.join("\n", lines);
    }
}
