package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DefaultPlannerClientContributor implements WorkspacePlannerClientContributor {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mknpc", "missing");

    @Override
    public ResourceLocation plannerId() {
        return ID;
    }

    @Override
    public void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal(
                "No topology UI is registered for planner " + editor.topologyPlannerId() + "."));
    }
}
