package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.RestoreWorkspaceBackupPacket;
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
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Workspace Backups"));
        MKText summary = addHeaderText(context, root, Component.literal(
                "Restore live workspace metadata from a backup manifest."));

        MKScrollView scrollView = addScrollBelowHeader(context, root, summary);

        MKStackLayoutVertical content = createContentStack(context);

        if (context.backupManifestFiles().isEmpty()) {
            MKText empty = context.makeWhiteText(Component.literal("No backups found for this workspace."));
            empty.setWidth(context.contentWidth());
            empty.setMultiline(true);
            content.addWidget(empty);
            content.addConstraintToWidget(MarginConstraint.LEFT, empty);
        } else {
            for (String fileName : context.backupManifestFiles()) {
                MKText fileLabel = context.makeWhiteText(Component.literal(fileName));
                fileLabel.setWidth(context.contentWidth());
                fileLabel.setMultiline(true);
                content.addWidget(fileLabel);
                content.addConstraintToWidget(MarginConstraint.LEFT, fileLabel);

                MKButton restore = new MKButton(Component.literal("Restore"), 180, 20);
                content.addWidget(restore);
                content.addConstraintToWidget(new CenterXConstraint(), restore);
                restore.setPressedCallback((button, mouseButton) -> {
                    PacketDistributor.sendToServer(new RestoreWorkspaceBackupPacket(context.anchor(), fileName));
                    return true;
                });
            }
        }

        finishScrollContent(context, scrollView, content);

        addBackButton(context, root, WorkspaceUtilitiesPage.ID);
        return root;
    }
}
