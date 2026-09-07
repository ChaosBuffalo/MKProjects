package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;

public class WorkspaceFormLinearRunsPage extends WorkspacePageBase {
    public static final String ID = "form_linear_runs";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        screen.switchToExistingState(WorkspaceFormFamiliesPage.ID);
        return new WorkspaceFormFamiliesPage().build(screen);
    }
}
