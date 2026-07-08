package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import net.minecraft.network.chat.Component;

public class WorkspaceGenerateConfirmPage extends WorkspacePageBase {
    public static final String ID = "generate_confirm";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Confirm Workspace Update"));
        addHeaderText(screen, root, Component.literal(
                "This change may rebuild workspace scaffold. Matching authored templates are preserved when possible; unmatched or affected pieces may be cleared and rebuilt."));
        addHeaderText(screen, root, Component.literal(screen.draftSession().workspaceId()));

        MKButton confirm = addBottomButton(screen, root, Component.literal("Apply Workspace Update"), 200, 1);
        confirm.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().send();
            return true;
        });

        MKButton cancel = addBottomButton(screen, root, Component.literal("Cancel"), 120, 0);
        cancel.setPressedCallback((button, mouseButton) -> {
            screen.switchToExistingState("form");
            return true;
        });
        return root;
    }
}

