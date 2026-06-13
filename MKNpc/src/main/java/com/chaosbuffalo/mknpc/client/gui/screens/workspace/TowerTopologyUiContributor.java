package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.resources.ResourceLocation;

public class TowerTopologyUiContributor implements WorkspaceTopologyUiContributor {
    private final TowerStackTopologyPanel towerStackPanel = new TowerStackTopologyPanel();

    @Override
    public ResourceLocation plannerId() {
        return MKWorkspaceTopologyProfile.TOWER_PLANNER_ID;
    }

    @Override
    public void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        towerStackPanel.addStackEditor(screen, content, editor, "tower.primary", "Primary Tower");
    }

    @Override
    public void addDefaultsLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                  WorkspaceDraftSession editor) {
        towerStackPanel.addStackEditor(screen, layout, editor, "tower.primary", "Primary Tower");
    }
}
