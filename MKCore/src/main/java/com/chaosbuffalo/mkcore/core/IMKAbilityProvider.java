package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IMKAbilityProvider {

    @Nullable
    MKAbilityInfo getAbilityInfo(ItemStack itemStack);
}
