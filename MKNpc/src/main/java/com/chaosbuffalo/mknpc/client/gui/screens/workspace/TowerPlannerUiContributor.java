package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerWorkspacePlanner;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TowerPlannerUiContributor implements WorkspacePlannerUiContributor {
    private final TowerStackTopologyPanel towerStackPanel = new TowerStackTopologyPanel();

    @Override
    public ResourceLocation plannerId() {
        return MKTowerWorkspacePlanner.PLANNER_ID;
    }

    @Override
    public WorkspacePlannerDraftAdapter createDraftAdapter() {
        return new TowerWorkspaceDraftAdapter();
    }

    @Override
    public void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        addTowerPaletteRows(screen, content, editor);
        towerStackPanel.addStackEditor(screen, content, editor, "tower.primary", "Primary Tower");
    }

    @Override
    public void addDefaultsLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                  WorkspaceDraftSession editor) {
        addTowerPaletteRows(screen, layout.settingsContent(), editor);
        towerStackPanel.addStackEditor(screen, layout, editor, "tower.primary", "Primary Tower");
    }

    @Override
    public void addWorkspaceOverviewSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                             WorkspaceDraftSession editor) {
        editor.ensureInitialized();
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal("Tower Planner"));
        addTowerPaletteRows(screen, content, editor);
        towerStackPanel.addStackEditor(screen, content, editor, "tower.primary", "Primary Tower");
    }

    @Override
    public void addWorkspaceOverviewLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                           WorkspaceDraftSession editor) {
        editor.ensureInitialized();
        WorkspaceTopologyUiSupport.addText(screen, layout.settingsContent(), Component.literal("Tower Planner"));
        addTowerPaletteRows(screen, layout.settingsContent(), editor);
        towerStackPanel.addStackEditor(screen, layout, editor, "tower.primary", "Primary Tower");
    }

    @Override
    public void addPlannerNodeLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                     WorkspaceDraftSession editor, String nodeId, String label,
                                     boolean topLevelPlanner) {
        towerStackPanel.addStackEditor(screen, layout, editor, nodeId, label, topLevelPlanner);
    }

    @Override
    public void addFloorPlanLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                   WorkspaceDraftSession editor, String stackId, String floorRole) {
        towerStackPanel.addFloorPlanEditor(screen, layout, editor, stackId, floorRole);
    }

    private void addTowerPaletteRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                     WorkspaceDraftSession editor) {
        screen.addPaletteOverrideRows(content, "Tower Palette Defaults", editor.palette(),
                editor.topologyGroupPaletteOverride("tower"),
                override -> editor.topologyGroupPaletteOverride("tower", override));
    }
}
