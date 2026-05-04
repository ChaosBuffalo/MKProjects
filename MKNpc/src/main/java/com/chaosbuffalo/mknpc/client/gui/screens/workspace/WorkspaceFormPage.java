package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import net.minecraft.network.chat.Component;

public class WorkspaceFormPage extends WorkspacePageBase {
    public static final String ID = "form";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Workspace Configuration"));
        addHeaderText(context, root, Component.literal(
                "Edit the workspace through focused v2 sections. Global screens handle naming, margins, materials, categories, family variants, openings, and hallway data."));
        addHeaderText(context, root, Component.literal(context.draftEditor().summary()));

        int firstButtonY = context.panelY() + 130;
        addNavigationButton(context, root, firstButtonY, "Identity & Bounds", "form_identity");
        addNavigationButton(context, root, firstButtonY + context.buttonHeight() + context.buttonGap(),
                "Materials", "form_materials");
        addNavigationButton(context, root, firstButtonY + ((context.buttonHeight() + context.buttonGap()) * 2),
                "Category Profiles", "form_categories");
        addNavigationButton(context, root, firstButtonY + ((context.buttonHeight() + context.buttonGap()) * 3),
                "Branch Variants", "form_families");
        addNavigationButton(context, root, firstButtonY + ((context.buttonHeight() + context.buttonGap()) * 4),
                "Opening Profiles", "form_openings");
        addNavigationButton(context, root, firstButtonY + ((context.buttonHeight() + context.buttonGap()) * 5),
                "Hallway Families", "form_hallways");

        if (context.draftEditor().hasExistingWorkspacePieces()) {
            MKButton backToWorkspace = addBottomButton(context, root,
                    Component.translatable("mknpc.workspace.button.back_to_workspace"), 180, 1);
            backToWorkspace.setPressedCallback((button, mouseButton) -> {
                context.switchToExistingState("workspace");
                return true;
            });
        }

        MKButton generate = addBottomButton(context, root,
                Component.translatable("mknpc.workspace.screen.generate"), 200, 0);
        generate.setPressedCallback((button, mouseButton) -> {
            context.draftEditor().submit();
            return true;
        });

        return root;
    }

    private void addNavigationButton(WorkspacePageContext context, MKLayout root, int y, String label,
                                     String targetState) {
        MKButton button = new MKButton(Component.literal(label), 220, context.buttonHeight());
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
        button.setY(y);
        button.setPressedCallback((pressedButton, mouseButton) -> {
            context.pushState(targetState);
            context.flagNeedSetup();
            return true;
        });
    }
}
