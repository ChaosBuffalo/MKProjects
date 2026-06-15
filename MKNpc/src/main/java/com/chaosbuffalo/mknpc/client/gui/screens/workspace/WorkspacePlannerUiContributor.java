package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface WorkspacePlannerUiContributor {
    ResourceLocation plannerId();

    default WorkspacePlannerDraftAdapter createDraftAdapter() {
        throw new IllegalStateException("No workspace draft adapter is registered for planner " + plannerId());
    }

    default void selectPlannerNode(WorkspaceDraftSession editor, String nodeId) {
    }

    default void addWorkspaceOverviewSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                              WorkspaceDraftSession editor) {
    }

    default void addWorkspaceOverviewLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                            WorkspaceDraftSession editor) {
        addWorkspaceOverviewSections(screen, layout.settingsContent(), editor);
    }

    default void addDefaultsLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                   WorkspaceDraftSession editor) {
        addDefaultsSections(screen, layout.settingsContent(), editor);
    }

    default void addPlannerNodeLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                      WorkspaceDraftSession editor, String nodeId, String label,
                                      boolean topLevelPlanner) {
        WorkspaceTopologyUiSupport.addText(screen, layout.settingsContent(), Component.literal(
                "No planner node UI is registered for " + nodeId + "."));
    }

    default void addFloorPlanLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                    WorkspaceDraftSession editor, String stackId, String floorRole) {
        WorkspaceTopologyUiSupport.addText(screen, layout.settingsContent(), Component.literal(
                "No floor planner UI is registered for " + stackId + "." + floorRole + "."));
    }

    void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                             WorkspaceDraftSession editor);
}
