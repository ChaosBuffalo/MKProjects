package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.RestoreWorkspaceBackupPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkspaceBackupPage implements WorkspacePage {
    public static final String ID = "backups";

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

        MKText title = context.makeWhiteText(Component.literal("Workspace Backups"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText summary = context.makeWhiteText(Component.literal(
                "Restore live workspace metadata from a backup manifest."));
        summary.setWidth(context.contentWidth());
        summary.setMultiline(true);
        root.addWidget(summary);
        root.addConstraintToWidget(StackConstraint.VERTICAL, summary);
        root.addConstraintToWidget(new CenterXConstraint(), summary);

        int scrollTop = context.scrollTopAfterHeader(root, summary);
        int scrollHeight = yPos + context.panelHeight() - context.bottomPadding() - context.buttonHeight() -
                12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(xPos + 10, scrollTop, context.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, context.contentWidth());
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

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

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        context.finalizeScrollView().accept(scrollView, id());

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + context.panelHeight() - context.bottomPadding() - context.buttonHeight());
        back.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState().accept("utilities");
            return true;
        });
        return root;
    }
}
