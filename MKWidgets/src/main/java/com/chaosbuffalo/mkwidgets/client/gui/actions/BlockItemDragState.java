package com.chaosbuffalo.mkwidgets.client.gui.actions;

import com.chaosbuffalo.mkwidgets.client.gui.instructions.DrawItemInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockItemDragState implements IDragState {
    private final ResourceLocation blockId;
    private final ItemStack previewStack;

    public BlockItemDragState(ResourceLocation blockId) {
        this.blockId = blockId;
        Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(Blocks.AIR);
        this.previewStack = block == Blocks.AIR ? ItemStack.EMPTY : new ItemStack(block);
    }

    public ResourceLocation getBlockId() {
        return blockId;
    }

    @Override
    public void updateDragState(Minecraft minecraft, int mouseX, int mouseY, IMKScreen screen) {
        if (!previewStack.isEmpty()) {
            screen.addPostRenderInstruction(new DrawItemInstruction(previewStack, mouseX, mouseY));
        }
    }
}
