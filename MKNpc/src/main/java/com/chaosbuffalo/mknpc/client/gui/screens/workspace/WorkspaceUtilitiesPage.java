package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantsForAllPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateAllWorkspaceStairsPacket;
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

public class WorkspaceUtilitiesPage implements WorkspacePage {
    public static final String ID = "utilities";

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

        MKText title = context.makeWhiteText(Component.literal("Utilities"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText summary = context.makeWhiteText(Component.literal(
                "Workspace-wide tools for live workspace maintenance."));
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

        MKButton blockSwap = new MKButton(Component.literal("Block Swap"), 180, 20);
        content.addWidget(blockSwap);
        content.addConstraintToWidget(new CenterXConstraint(), blockSwap);
        blockSwap.setPressedCallback((button, mouseButton) -> {
            context.pushState().accept("block_swap");
            context.flagNeedSetup().run();
            return true;
        });

        MKButton backups = new MKButton(Component.literal("Backups (" +
                context.backupManifestFiles().size() + ")"), 180, 20);
        content.addWidget(backups);
        content.addConstraintToWidget(new CenterXConstraint(), backups);
        backups.setPressedCallback((button, mouseButton) -> {
            context.pushState().accept(WorkspaceBackupPage.ID);
            context.flagNeedSetup().run();
            return true;
        });

        if (context.workspace().pieces().stream().anyMatch(context.supportsStairGeneration())) {
            MKButton generateAllStairs = new MKButton(Component.literal("Generate All Stairs"), 180, 20);
            content.addWidget(generateAllStairs);
            content.addConstraintToWidget(new CenterXConstraint(), generateAllStairs);
            generateAllStairs.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new GenerateAllWorkspaceStairsPacket(context.anchor()));
                return true;
            });
        }

        MKButton addCopyForAll = new MKButton(
                Component.translatable("mknpc.workspace.button.add_copy_for_all"), 180, 20);
        content.addWidget(addCopyForAll);
        content.addConstraintToWidget(new CenterXConstraint(), addCopyForAll);
        addCopyForAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new AddWorkspaceVariantsForAllPacket(context.anchor()));
            return true;
        });

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        context.finalizeScrollView().accept(scrollView, id());

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + context.panelHeight() - context.bottomPadding() - context.buttonHeight());
        back.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState().accept("workspace");
            return true;
        });
        return root;
    }
}
