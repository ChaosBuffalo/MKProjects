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

        addTitle(screen, root, Component.literal("Confirm Regenerate"));
        addHeaderText(screen, root, Component.literal(
                "This change is not covered by a safe live mutation. Regenerating will rebuild the workspace scaffold and overwrite existing authored workspace blocks."));
        addHeaderText(screen, root, Component.literal(screen.draftSession().workspaceId()));

        MKButton confirm = addBottomButton(screen, root, Component.literal("Regenerate Workspace"), 200, 1);
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

