package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantsForAllPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateAllWorkspaceStairsPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkspaceUtilitiesPage extends WorkspacePageBase {
    public static final String ID = "utilities";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Utilities"));
        MKText summary = addHeaderText(context, root, Component.literal(
                "Workspace-wide tools for live workspace maintenance."));

        MKScrollView scrollView = addScrollBelowHeader(context, root, summary);

        MKStackLayoutVertical content = createContentStack(context);

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

        finishScrollContent(context, scrollView, content);

        addBackButton(context, root, "workspace");
        return root;
    }
}
