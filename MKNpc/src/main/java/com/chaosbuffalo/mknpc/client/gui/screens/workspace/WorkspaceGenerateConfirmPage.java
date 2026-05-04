package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

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
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Confirm Regenerate"));
        addHeaderText(context, root, Component.literal(
                "This change is not covered by a safe live mutation. Regenerating will rebuild the workspace scaffold and overwrite existing authored workspace blocks."));
        addHeaderText(context, root, Component.literal(context.draftWorkspaceId().get()));

        MKButton confirm = addBottomButton(context, root, Component.literal("Regenerate Workspace"), 200, 1);
        confirm.setPressedCallback((button, mouseButton) -> {
            context.sendWorkspaceDraft().run();
            return true;
        });

        MKButton cancel = addBottomButton(context, root, Component.literal("Cancel"), 120, 0);
        cancel.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState().accept("form");
            return true;
        });
        return root;
    }
}
