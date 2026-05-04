package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
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
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Utilities"));
        MKText summary = addHeaderText(screen, root, Component.literal(
                "Workspace-wide tools for live workspace maintenance."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, summary);

        MKStackLayoutVertical content = createContentStack(screen);

        MKButton blockSwap = new MKButton(Component.literal("Block Swap"), 180, 20);
        content.addWidget(blockSwap);
        content.addConstraintToWidget(new CenterXConstraint(), blockSwap);
        blockSwap.setPressedCallback((button, mouseButton) -> {
            screen.pushState("block_swap");
            screen.flagNeedSetup();
            return true;
        });

        MKButton backups = new MKButton(Component.literal("Backups (" +
                screen.backupManifestFiles().size() + ")"), 180, 20);
        content.addWidget(backups);
        content.addConstraintToWidget(new CenterXConstraint(), backups);
        backups.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceBackupPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        if (screen.workspace().pieces().stream().anyMatch(screen::supportsStairGeneration)) {
            MKButton generateAllStairs = new MKButton(Component.literal("Generate All Stairs"), 180, 20);
            content.addWidget(generateAllStairs);
            content.addConstraintToWidget(new CenterXConstraint(), generateAllStairs);
            generateAllStairs.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new GenerateAllWorkspaceStairsPacket(screen.anchor()));
                return true;
            });
        }

        MKButton addCopyForAll = new MKButton(
                Component.translatable("mknpc.workspace.button.add_copy_for_all"), 180, 20);
        content.addWidget(addCopyForAll);
        content.addConstraintToWidget(new CenterXConstraint(), addCopyForAll);
        addCopyForAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new AddWorkspaceVariantsForAllPacket(screen.anchor()));
            return true;
        });

        finishScrollContent(screen, scrollView, content);

        addBackButton(screen, root, "workspace");
        return root;
    }
}
