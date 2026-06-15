package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspacePlannerNodePage extends WorkspacePageBase {
    public static final String ID = "planner_node";

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

        int contentTop = screen.scrollTopAfterHeader(root, summary);
        int contentHeight = screen.panelY() + screen.panelHeight() - screen.bottomPadding() -
                screen.buttonHeight() - 12 - contentTop;
        WorkspacePlannerLayout layout = addPlannerLayout(screen, root, contentTop, contentHeight);
        boolean topLevelTowerPlanner = MKWorkspaceTopologyProfile.TOWER_PLANNER_ID.equals(
                screen.draftSession().topologyPlannerId());
        WorkspacePlannerClientRegistry.getPlannerUi(screen.draftSession().topologyPlannerId())
                .addPlannerNodeLayout(screen, layout, screen.draftSession(), stackId, label, topLevelTowerPlanner);
        finishPlannerLayout(screen, layout);
        addBackButton(screen, root, WorkspaceManagePage.ID);
        return root;
    }
}
