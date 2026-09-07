package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequests;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceSimpleChangeOperation;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspaceImportPage extends WorkspacePageBase {
    public static final String ID = "import";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Load Existing Workspace"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Choose an exported workspace manifest to rehydrate at this dev block."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);

        MKStackLayoutVertical content = createContentStack(screen);
        for (String manifestId : screen.importManifestIds()) {
            MKButton manifestButton = new MKButton(Component.literal(manifestId), screen.contentWidth() - 8, 20);
            content.addWidget(manifestButton);
            manifestButton.setPressedCallback((button, mouseButton) -> {
                screen.requestWorkspaceChange(MKWorkspaceChangeRequests.target(
                        MKWorkspaceSimpleChangeOperation.Kind.IMPORT, screen.anchor(), manifestId));
                return true;
            });
        }
        finishScrollContent(screen, scrollView, content);

        addBackButton(screen, root, WorkspaceHomePage.ID);
        return root;
    }
}
