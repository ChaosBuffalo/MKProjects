package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspaceHomePage implements WorkspacePage {
    public static final String ID = "home";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        int xPos = context.panelX();
        int yPos = context.panelY();
        MKLayout root = new MKLayout(xPos, yPos, context.panelWidth(), context.panelHeight());
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = context.makeWhiteText(Component.translatable("mknpc.workspace.screen.title"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = context.makeWhiteText(Component.literal("Create a new workspace or load an exported one."));
        helpText.setWidth(context.contentWidth());
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        MKButton createNew = new MKButton(Component.literal("Create New Workspace"), 220, 20);
        root.addWidget(createNew);
        root.addConstraintToWidget(new CenterXConstraint(), createNew);
        createNew.setY(yPos + 120);
        createNew.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState().accept("form");
            return true;
        });

        MKButton loadExisting = new MKButton(Component.literal("Load Existing Workspace"), 220, 20);
        root.addWidget(loadExisting);
        root.addConstraintToWidget(new CenterXConstraint(), loadExisting);
        loadExisting.setY(yPos + 120 + context.buttonHeight() + context.buttonGap());
        loadExisting.setPressedCallback((button, mouseButton) -> {
            if (!context.importManifestIds().isEmpty()) {
                context.switchToExistingState().accept(WorkspaceImportPage.ID);
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
