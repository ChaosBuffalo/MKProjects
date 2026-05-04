package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

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
    public MKLayout build(WorkspacePageContext context) {
        int yPos = context.panelY();
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.translatable("mknpc.workspace.screen.title"));
        addHeaderText(context, root, Component.literal("Create a new workspace or load an exported one."));

        MKButton createNew = new MKButton(Component.literal("Create New Workspace"), 220, 20);
        root.addWidget(createNew);
        root.addConstraintToWidget(new CenterXConstraint(), createNew);
        createNew.setY(yPos + 120);
        createNew.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState("form");
            return true;
        });

        MKButton loadExisting = new MKButton(Component.literal("Load Existing Workspace"), 220, 20);
        root.addWidget(loadExisting);
        root.addConstraintToWidget(new CenterXConstraint(), loadExisting);
        loadExisting.setY(yPos + 120 + context.buttonHeight() + context.buttonGap());
        loadExisting.setPressedCallback((button, mouseButton) -> {
            if (!context.importManifestIds().isEmpty()) {
                context.switchToExistingState(WorkspaceImportPage.ID);
            }
            return true;
        });

        if (context.importManifestIds().isEmpty()) {
            MKText emptyText = context.makeWhiteText(Component.literal("No exported workspace manifests found."));
            emptyText.setWidth(context.contentWidth());
            emptyText.setMultiline(true);
            emptyText.setY(yPos + 120 + ((context.buttonHeight() + context.buttonGap()) * 2));
            root.addWidget(emptyText);
            root.addConstraintToWidget(new CenterXConstraint(), emptyText);
        }

        return root;
    }
}
