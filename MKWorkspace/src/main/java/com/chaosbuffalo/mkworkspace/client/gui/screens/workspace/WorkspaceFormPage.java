package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
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
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Workspace Configuration"));
        addHeaderText(screen, root, Component.literal(
                "Edit the workspace through focused v2 sections. Global screens handle naming, margins, template families, openings, and insert data."));
        addHeaderText(screen, root, Component.literal(screen.draftSession().summary()));

        int firstButtonY = screen.panelY() + 130;
        addNavigationButton(screen, root, firstButtonY, "Identity & Bounds", "form_identity");
        addNavigationButton(screen, root, firstButtonY + screen.buttonHeight() + screen.buttonGap(),
                "Template Families", WorkspaceFormFamiliesPage.ID);
        addNavigationButton(screen, root, firstButtonY + ((screen.buttonHeight() + screen.buttonGap()) * 2),
                "Opening Profiles", "form_openings");
        addNavigationButton(screen, root, firstButtonY + ((screen.buttonHeight() + screen.buttonGap()) * 3),
                "Insert Families", WorkspaceFormInsertFamiliesPage.ID);

        MKButton resetDefaults = new MKButton(Component.literal("Reset Planner Defaults"), 220,
                screen.buttonHeight());
        root.addWidget(resetDefaults);
        root.addConstraintToWidget(new CenterXConstraint(), resetDefaults);
        resetDefaults.setY(firstButtonY + ((screen.buttonHeight() + screen.buttonGap()) * 4));
        resetDefaults.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().resetCurrentTopologyDefaults();
            screen.flagNeedSetup();
            return true;
        });

        if (screen.draftSession().hasExistingWorkspacePieces()) {
            MKButton backToWorkspace = addBottomButton(screen, root,
                    Component.translatable("mknpc.workspace.button.back_to_workspace"), 180, 1);
            backToWorkspace.setPressedCallback((button, mouseButton) -> {
                screen.switchToExistingState("workspace");
                return true;
            });
        }

        MKButton generate = addBottomButton(screen, root,
                Component.translatable("mknpc.workspace.screen.generate"), 200, 0);
        generate.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().submit();
            return true;
        });

        return root;
    }

    private void addNavigationButton(MKWorkspaceScreen screen, MKLayout root, int y, String label,
                                     String targetState) {
        MKButton button = new MKButton(Component.literal(label), 220, screen.buttonHeight());
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
        button.setY(y);
        button.setPressedCallback((pressedButton, mouseButton) -> {
            screen.pushState(targetState);
            screen.flagNeedSetup();
            return true;
        });
    }
}

