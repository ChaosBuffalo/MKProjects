package com.chaosbuffalo.mkcore.entities;

import net.minecraft.world.item.ItemStack;

public interface IMKRenderAsItem {

    ItemStack getItem();

    default float getScale() {
        return 1.0f;
    }
}
