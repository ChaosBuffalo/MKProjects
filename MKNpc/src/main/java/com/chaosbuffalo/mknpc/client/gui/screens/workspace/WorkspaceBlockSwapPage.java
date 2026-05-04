package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

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
    public MKLayout build(WorkspacePageContext context) {
        int xPos = context.panelX();
        int yPos = context.panelY();
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Block Swap"));

        if (context.blockSwapSourceBlock().get() == null) {
            context.setBlockSwapSourceBlock().accept(context.workspace().palette().wallBlock());
        }
        if (context.blockSwapTargetBlock().get() == null) {
            context.setBlockSwapTargetBlock().accept(context.workspace().palette().floorBlock());
        }

        addHeaderText(context, root, Component.literal(
                "Choose source and target blocks to replace across the live workspace."));

        int rowTop = yPos + 112;
        context.addBlockPickerRow().add(root, xPos, rowTop, "Source", context.blockSwapSourceBlock().get(),
                context.setBlockSwapSourceBlock(), false);
        context.addBlockPickerRow().add(root, xPos, rowTop + 42, "Target", context.blockSwapTargetBlock().get(),
                context.setBlockSwapTargetBlock(), false);

        MKButton swapBlocks = addBottomButton(context, root, Component.literal("Swap Blocks"), 180, 1);
        swapBlocks.setPressedCallback((button, mouseButton) -> {
            ResourceLocation sourceBlock = context.blockSwapSourceBlock().get();
            ResourceLocation targetBlock = context.blockSwapTargetBlock().get();
            if (!sourceBlock.equals(ResourceLocation.withDefaultNamespace("air")) &&
                    !sourceBlock.equals(targetBlock)) {
                PacketDistributor.sendToServer(new SwapWorkspaceBlockPacket(
                        context.anchor(), sourceBlock, targetBlock));
            }
            return true;
        });

        addBackButton(context, root, WorkspaceUtilitiesPage.ID);
        return root;
    }
}
