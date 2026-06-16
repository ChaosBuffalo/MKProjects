package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspaceFloorPlanPage extends WorkspacePageBase {
    public static final String ID = "floor_plan";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        String stackId = screen.selectedFloorPlanStackId();
        String sectionKey = screen.selectedFloorPlanSectionKey();
        if (stackId == null || stackId.isBlank() || sectionKey == null || sectionKey.isBlank()) {
            screen.goBackOrSwitchTo(WorkspacePlannerNodePage.ID);
            return new WorkspacePlannerNodePage().build(screen);
        }

        MKLayout root = createPanel(screen);
        String stackLabel = WorkspacePieceDisplay.formatTopologyLabel(stackId);
        String sectionLabel = WorkspacePieceDisplay.formatTopologyLabel(sectionKey);
        addTitle(screen, root, Component.literal(sectionLabel + " Floor Plan"));
        MKText summary = addHeaderText(screen, root, Component.literal(stackLabel + " / " + sectionLabel));

        int contentTop = screen.scrollTopAfterHeader(root, summary);
        int contentHeight = screen.panelY() + screen.panelHeight() - screen.bottomPadding() -
                screen.buttonHeight() - 12 - contentTop;
        WorkspacePlannerLayout layout = addPlannerLayout(screen, root, contentTop, contentHeight);
        WorkspacePlannerClientRegistry.getClientContributor(screen.draftSession().topologyPlannerId())
                .addFloorPlanLayout(screen, layout, screen.draftSession(), stackId, sectionKey);
        finishPlannerLayout(screen, layout);
        addApplyBackButtonRow(screen, root, WorkspacePlannerNodePage.ID);
        return root;
    }
}
