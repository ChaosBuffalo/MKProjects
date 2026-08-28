package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceRelayoutImpact;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

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

    protected WorkspacePlannerLayout addPlannerLayout(MKWorkspaceScreen screen, MKLayout root,
                                                      int top, int height) {
        int gap = 12;
        int paneWidth = screen.contentWidth();
        int layoutWidth = (paneWidth * 2) + gap;
        int left = screen.panelX() + (screen.panelWidth() - layoutWidth) / 2;
        int settingsLeft = left + paneWidth + gap;

        MKScrollView previewScrollView = new MKScrollView(left, top, paneWidth, height);
        previewScrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(previewScrollView);

        MKScrollView settingsScrollView = new MKScrollView(settingsLeft, top, paneWidth, height);
        settingsScrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(settingsScrollView);

        MKStackLayoutVertical previewContent = new MKStackLayoutVertical(0, 0, paneWidth);
        previewContent.setMargins(4, 4, 4, 4);
        previewContent.setPaddingTop(4).setPaddingBot(4);

        MKStackLayoutVertical settingsContent = new MKStackLayoutVertical(0, 0, paneWidth);
        settingsContent.setMargins(4, 4, 4, 4);
        settingsContent.setPaddingTop(4).setPaddingBot(4);
        return new WorkspacePlannerLayout(previewContent, settingsContent, previewScrollView, settingsScrollView);
    }

    protected void finishPlannerLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout) {
        layout.finish(screen, id());
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

    protected MKStackLayoutHorizontal addApplyBackButtonRow(MKWorkspaceScreen screen, MKLayout root,
                                                            String backTargetState) {
        return addApplyBackButtonRow(screen, root, () -> screen.goBackOrSwitchTo(backTargetState));
    }

    protected MKStackLayoutHorizontal addApplyBackButtonRow(MKWorkspaceScreen screen, MKLayout root,
                                                            Runnable backAction) {
        MKStackLayoutHorizontal row = new MKStackLayoutHorizontal(0, 0, screen.buttonHeight());
        row.setPaddingLeft(4).setPaddingRight(4);

        MKButton apply = new MKButton(Component.literal("Apply Changes"), 160, screen.buttonHeight()) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && screen.draftSession().dirty();
            }
        };
        apply.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().submit();
            return true;
        });
        row.addWidget(apply);

        MKButton back = new MKButton(Component.literal("Back"), 120, screen.buttonHeight());
        back.setPressedCallback((button, mouseButton) -> {
            backAction.run();
            return true;
        });
        row.addWidget(back);

        root.addWidget(row);
        root.addConstraintToWidget(new CenterXConstraint(), row);
        row.setY(screen.panelY() + screen.panelHeight() - screen.bottomPadding() - screen.buttonHeight());
        return row;
    }

    protected MKText addText(MKWorkspaceScreen screen, MKStackLayoutVertical content, String text) {
        MKText widget = screen.makeWhiteText(Component.literal(text));
        widget.setWidth(screen.contentWidth());
        widget.setMultiline(true);
        content.addWidget(widget);
        content.addConstraintToWidget(MarginConstraint.LEFT, widget);
        return widget;
    }

    protected void addRelayoutImpactReport(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                           MKWorkspaceInvalidationReport report) {
        for (String line : relayoutImpactReportLines(report)) {
            addText(screen, content, line);
        }
    }

    protected List<String> relayoutImpactReportLines(MKWorkspaceInvalidationReport report) {
        List<MKWorkspaceRelayoutImpact> impacts = report.relayoutImpacts();
        if (impacts.isEmpty()) {
            return List.of();
        }
        ArrayList<String> lines = new ArrayList<>();
        long unchanged = countImpacts(impacts, "preserved");
        long moved = countImpacts(impacts, "moved");
        long expanded = countImpacts(impacts, "expanded");
        long added = countImpacts(impacts, "new");
        long rebuilt = countImpacts(impacts, "rebuild");
        long removed = countImpacts(impacts, "removed");
        long patched = countImpacts(impacts, "scaffold_patch");
        lines.add("These changes will add " + added + " pieces, rebuild " + rebuilt +
                " pieces, remove " + removed + " pieces, move " + moved + " pieces, expand " +
                expanded + " pieces, patch " + patched + " pieces, and leave " + unchanged +
                " pieces unchanged.");

        addImpactSectionLines(lines, "Added", impacts, "new");
        addImpactSectionLines(lines, "Rebuilt", impacts, "rebuild");
        addImpactSectionLines(lines, "Removed", impacts, "removed");
        addImpactSectionLines(lines, "Moved", impacts, "moved");
        addImpactSectionLines(lines, "Expanded", impacts, "expanded");
        addImpactSectionLines(lines, "Patched", impacts, "scaffold_patch");
        addImpactSectionLines(lines, "Unchanged", impacts, "preserved");
        return List.copyOf(lines);
    }

    private long countImpacts(List<MKWorkspaceRelayoutImpact> impacts, String outcome) {
        return impacts.stream()
                .filter(impact -> outcome.equals(impact.outcome()))
                .count();
    }

    private void addImpactSection(MKWorkspaceScreen screen, MKStackLayoutVertical content, String title,
                                  List<MKWorkspaceRelayoutImpact> impacts, String outcome) {
        for (String line : impactSectionLines(title, impacts, outcome)) {
            addText(screen, content, line);
        }
    }

    private void addImpactSectionLines(List<String> lines, String title, List<MKWorkspaceRelayoutImpact> impacts,
                                       String outcome) {
        lines.addAll(impactSectionLines(title, impacts, outcome));
    }

    private List<String> impactSectionLines(String title, List<MKWorkspaceRelayoutImpact> impacts, String outcome) {
        List<MKWorkspaceRelayoutImpact> matching = impacts.stream()
                .filter(impact -> outcome.equals(impact.outcome()))
                .toList();
        if (matching.isEmpty()) {
            return List.of();
        }
        ArrayList<String> lines = new ArrayList<>();
        lines.add(title);
        int shown = Math.min(10, matching.size());
        for (int i = 0; i < shown; i++) {
            lines.add("- " + formatRelayoutImpact(matching.get(i)));
        }
        int hidden = matching.size() - shown;
        if (hidden > 0) {
            lines.add("- and " + hidden + " more");
        }
        return List.copyOf(lines);
    }

    private String formatRelayoutImpact(MKWorkspaceRelayoutImpact impact) {
        String variantLabel = impact.variantIndex() == 0 ? "template" : "variant " + impact.variantIndex();
        String pieceLabel = impact.baseName();
        if (pieceLabel == null || pieceLabel.isBlank()) {
            pieceLabel = impact.pieceName();
        } else if (!pieceLabel.equals(impact.pieceName()) && impact.pieceName() != null &&
                !impact.pieceName().isBlank()) {
            pieceLabel += " / " + impact.pieceName();
        }
        return pieceLabel + " (" + variantLabel + ") - " + impact.reason();
    }
}
