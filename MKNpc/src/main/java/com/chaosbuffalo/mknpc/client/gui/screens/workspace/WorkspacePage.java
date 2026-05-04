package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;

public interface WorkspacePage {
    String id();

    MKLayout build(WorkspacePageContext context);
}
