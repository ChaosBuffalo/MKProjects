package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequests;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceSimpleChangeOperation;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import net.minecraft.network.chat.Component;

public class WorkspaceDeleteConfirmPage extends WorkspacePageBase {
    public static final String ID = "delete_confirm";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Confirm Delete"));
        addHeaderText(screen, root, Component.literal(
                "This will clear the authored workspace area, delete the workspace registry entry, and remove the workspace block."));
        addHeaderText(screen, root, Component.literal(screen.workspace().namespace() + ":" +
                screen.workspace().structureName()));

        MKButton confirm = addBottomButton(screen, root, Component.literal("Delete Workspace"), 180, 1);
        confirm.setPressedCallback((button, mouseButton) -> {
            screen.requestWorkspaceChange(MKWorkspaceChangeRequests.simple(
                    MKWorkspaceSimpleChangeOperation.Kind.DELETE, screen.anchor()));
            return true;
        });

        MKButton cancel = addBottomButton(screen, root, Component.literal("Cancel"), 120, 0);
        cancel.setPressedCallback((button, mouseButton) -> {
            screen.switchToExistingState(WorkspaceUtilitiesPage.ID);
            return true;
        });
        return root;
    }
}
