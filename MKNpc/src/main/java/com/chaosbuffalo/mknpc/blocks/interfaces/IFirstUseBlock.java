package com.chaosbuffalo.mknpc.blocks.interfaces;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public interface IFirstUseBlock {


    InteractionResult onFirstUse(ItemStack stack, UseOnContext context);
}
