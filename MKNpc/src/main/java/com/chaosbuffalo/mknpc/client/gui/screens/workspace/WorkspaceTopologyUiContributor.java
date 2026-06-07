package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.resources.ResourceLocation;

public interface WorkspaceTopologyUiContributor {
    ResourceLocation plannerId();

    void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                             WorkspaceDraftSession editor);
}
