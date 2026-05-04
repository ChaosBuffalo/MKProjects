package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.SwapWorkspaceBlockPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkspaceBlockSwapPage implements WorkspacePage {
    public static final String ID = "block_swap";

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

        MKText title = context.makeWhiteText(Component.literal("Block Swap"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        if (context.blockSwapSourceBlock().get() == null) {
            context.setBlockSwapSourceBlock().accept(context.workspace().palette().wallBlock());
        }
        if (context.blockSwapTargetBlock().get() == null) {
            context.setBlockSwapTargetBlock().accept(context.workspace().palette().floorBlock());
        }

        MKText summary = context.makeWhiteText(Component.literal(
                "Choose source and target blocks to replace across the live workspace."));
        summary.setWidth(context.contentWidth());
        summary.setMultiline(true);
        root.addWidget(summary);
        root.addConstraintToWidget(StackConstraint.VERTICAL, summary);
        root.addConstraintToWidget(new CenterXConstraint(), summary);

        int rowTop = yPos + 112;
        context.addBlockPickerRow().add(root, xPos, rowTop, "Source", context.blockSwapSourceBlock().get(),
                context.setBlockSwapSourceBlock(), false);
        context.addBlockPickerRow().add(root, xPos, rowTop + 42, "Target", context.blockSwapTargetBlock().get(),
                context.setBlockSwapTargetBlock(), false);

        MKButton swapBlocks = new MKButton(Component.literal("Swap Blocks"), 180, 20);
        root.addWidget(swapBlocks);
        root.addConstraintToWidget(new CenterXConstraint(), swapBlocks);
        swapBlocks.setY(yPos + context.panelHeight() - context.bottomPadding() - context.buttonHeight() -
                context.buttonGap() - context.buttonHeight());
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

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + context.panelHeight() - context.bottomPadding() - context.buttonHeight());
        back.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState().accept(WorkspaceUtilitiesPage.ID);
            return true;
        });
        return root;
    }
}
