package com.chaosbuffalo.mknpc.items;

import com.chaosbuffalo.mknpc.blocks.interfaces.IFirstUseBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public class FirstUseBlockItem extends BlockItem {

    public FirstUseBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (getBlock() instanceof IFirstUseBlock firstUseBlock) {
            return firstUseBlock.onFirstUse(stack, context);
        }
        return super.onItemUseFirst(stack, context);
    }
}
