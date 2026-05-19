package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorkspaceFormLinearRunsPage extends WorkspacePageBase {
    public static final String ID = "form_linear_runs";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Linear Run Families"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Choose a linear run family and edit it on its own screen. Each run defines its slot, kind, opening profile, dimensions, slope, path usage, and palette overrides."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKWorkspaceLinearRunFamilyDefinition> linearRuns = editor.linearRunFamilies();

        for (int i = 0; i < linearRuns.size(); i++) {
            int index = i;
            MKWorkspaceLinearRunFamilyDefinition linearRun = linearRuns.get(index);
            MKText header = screen.makeWhiteText(Component.literal(linearRun.linearRunId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = screen.makeWhiteText(Component.literal(
                    formatTopologyLabel(linearRun.kind().getSerializedName()) + "  |  " +
                            linearRun.topologySlotId() + "  |  " + linearRun.openingProfileId() + "  |  " + linearRun.length() + "x" +
                            linearRun.interiorWidth() + "x" + linearRun.interiorHeight() +
                            "  |  slope " + linearRun.slopeDelta() + "  |  " +
                            describePathAccess(linearRun.allowOnMainPath(), linearRun.allowOnBranchPath())));
            summary.setWidth(screen.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Run"), 180, screen.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedLinearRunIndex(index);
                screen.pushState(WorkspaceFormLinearRunDetailPage.ID);
                screen.flagNeedSetup();
                return true;
            });
        }

        finishScrollContent(screen, scrollView, content);

        MKButton addLinearRun = addBottomButton(screen, root, Component.literal("Add Run"), 180, 1);
        addLinearRun.setPressedCallback((button, mouseButton) -> {
            editor.selectedLinearRunIndex(editor.addLinearRunFamily());
            screen.pushState(WorkspaceFormLinearRunDetailPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }

    private String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }

    private String describePathAccess(boolean main, boolean branch) {
        if (main && branch) {
            return "main + branch";
        }
        if (main) {
            return "main only";
        }
        if (branch) {
            return "branch only";
        }
        return "disabled";
    }
}


