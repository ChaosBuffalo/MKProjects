package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.SwapWorkspaceBlockPacket;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkspaceBlockSwapPage extends WorkspacePageBase {
    public static final String ID = "block_swap";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        int xPos = screen.panelX();
        int yPos = screen.panelY();
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Block Swap"));

        if (screen.blockSwapSourceBlock() == null) {
            screen.setBlockSwapSourceBlock(screen.workspace().palette().wallBlock());
        }
        if (screen.blockSwapTargetBlock() == null) {
            screen.setBlockSwapTargetBlock(screen.workspace().palette().floorBlock());
        }

        addHeaderText(screen, root, Component.literal(
                "Choose source and target blocks to replace across the live workspace."));

        int rowTop = yPos + 112;
        screen.addBlockPickerRow(root, xPos, rowTop, "Source", screen.blockSwapSourceBlock(),
                screen::setBlockSwapSourceBlock, false);
        screen.addBlockPickerRow(root, xPos, rowTop + 42, "Target", screen.blockSwapTargetBlock(),
                screen::setBlockSwapTargetBlock, false);

        MKButton swapBlocks = addBottomButton(screen, root, Component.literal("Swap Blocks"), 180, 1);
        swapBlocks.setPressedCallback((button, mouseButton) -> {
            ResourceLocation sourceBlock = screen.blockSwapSourceBlock();
            ResourceLocation targetBlock = screen.blockSwapTargetBlock();
            if (!sourceBlock.equals(ResourceLocation.withDefaultNamespace("air")) &&
                    !sourceBlock.equals(targetBlock)) {
                PacketDistributor.sendToServer(new SwapWorkspaceBlockPacket(
                        screen.anchor(), sourceBlock, targetBlock));
            }
            return true;
        });

        addBackButton(screen, root, WorkspaceUtilitiesPage.ID);
        return root;
    }
}
