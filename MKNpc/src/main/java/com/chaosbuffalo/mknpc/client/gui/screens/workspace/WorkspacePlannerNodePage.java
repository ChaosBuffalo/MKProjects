package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspacePlannerNodePage extends WorkspacePageBase {
    public static final String ID = "planner_node";

    private final TowerStackTopologyPanel towerStackPanel = new TowerStackTopologyPanel();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        String stackId = screen.selectedPlannerStackId();
        if (stackId == null || stackId.isBlank()) {
            screen.goBackOrSwitchTo(WorkspaceManagePage.ID);
            return new WorkspaceManagePage().build(screen);
        }

        MKLayout root = createPanel(screen);
        String label = WorkspacePieceDisplay.formatTopologyLabel(stackId);
        addTitle(screen, root, Component.literal(label));
        MKText summary = addHeaderText(screen, root, Component.literal("Tower stack controls for " + label + "."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, summary);
        MKStackLayoutVertical content = createContentStack(screen);
        towerStackPanel.addStackEditor(screen, content, screen.draftSession(), stackId, label);
        finishScrollContent(screen, scrollView, content);
        addBackButton(screen, root, WorkspaceManagePage.ID);
        return root;
    }
}
