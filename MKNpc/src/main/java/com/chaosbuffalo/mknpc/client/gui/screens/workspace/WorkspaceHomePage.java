package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspaceHomePage extends WorkspacePageBase {
    public static final String ID = "home";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        int yPos = screen.panelY();
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.translatable("mknpc.workspace.screen.title"));
        addHeaderText(screen, root, Component.literal("Create a new workspace or load an exported one."));

        MKButton createNew = new MKButton(Component.literal("Create New Workspace"), 220, 20);
        root.addWidget(createNew);
        root.addConstraintToWidget(new CenterXConstraint(), createNew);
        createNew.setY(yPos + 120);
        createNew.setPressedCallback((button, mouseButton) -> {
            screen.switchToExistingState("form");
            return true;
        });

        MKButton loadExisting = new MKButton(Component.literal("Load Existing Workspace"), 220, 20);
        root.addWidget(loadExisting);
        root.addConstraintToWidget(new CenterXConstraint(), loadExisting);
        loadExisting.setY(yPos + 120 + screen.buttonHeight() + screen.buttonGap());
        loadExisting.setPressedCallback((button, mouseButton) -> {
            if (!screen.importManifestIds().isEmpty()) {
                screen.switchToExistingState(WorkspaceImportPage.ID);
            }
            return true;
        });

        if (screen.importManifestIds().isEmpty()) {
            MKText emptyText = screen.makeWhiteText(Component.literal("No exported workspace manifests found."));
            emptyText.setWidth(screen.contentWidth());
            emptyText.setMultiline(true);
            emptyText.setY(yPos + 120 + ((screen.buttonHeight() + screen.buttonGap()) * 2));
            root.addWidget(emptyText);
            root.addConstraintToWidget(new CenterXConstraint(), emptyText);
        }

        return root;
    }
}
