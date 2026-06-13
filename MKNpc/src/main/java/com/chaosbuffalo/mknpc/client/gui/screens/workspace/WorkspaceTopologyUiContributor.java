package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.resources.ResourceLocation;

public interface WorkspaceTopologyUiContributor {
    ResourceLocation plannerId();

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

    void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                             WorkspaceDraftSession editor);
}
