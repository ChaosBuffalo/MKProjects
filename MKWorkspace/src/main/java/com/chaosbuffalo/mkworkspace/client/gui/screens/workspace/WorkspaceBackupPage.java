package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.RestoreWorkspaceBackupPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkspaceBackupPage extends WorkspacePageBase {
    public static final String ID = "backups";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Workspace Backups"));
        MKText summary = addHeaderText(screen, root, Component.literal(
                "Restore live workspace metadata from a backup manifest."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, summary);

        MKStackLayoutVertical content = createContentStack(screen);

        if (screen.backupManifestFiles().isEmpty()) {
            MKText empty = screen.makeWhiteText(Component.literal("No backups found for this workspace."));
            empty.setWidth(screen.contentWidth());
            empty.setMultiline(true);
            content.addWidget(empty);
            content.addConstraintToWidget(MarginConstraint.LEFT, empty);
        } else {
            for (String fileName : screen.backupManifestFiles()) {
                MKText fileLabel = screen.makeWhiteText(Component.literal(fileName));
                fileLabel.setWidth(screen.contentWidth());
                fileLabel.setMultiline(true);
                content.addWidget(fileLabel);
                content.addConstraintToWidget(MarginConstraint.LEFT, fileLabel);

                MKButton restore = new MKButton(Component.literal("Restore"), 180, 20);
                content.addWidget(restore);
                content.addConstraintToWidget(new CenterXConstraint(), restore);
                restore.setPressedCallback((button, mouseButton) -> {
                    PacketDistributor.sendToServer(new RestoreWorkspaceBackupPacket(screen.anchor(), fileName));
                    return true;
                });
            }
        }

        finishScrollContent(screen, scrollView, content);

        addBackButton(screen, root, WorkspaceUtilitiesPage.ID);
        return root;
    }
}
