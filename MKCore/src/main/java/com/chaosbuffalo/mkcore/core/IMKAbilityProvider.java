package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IMKAbilityProvider {
    @Nullable
    MKAbility getAbility(ItemStack item);
}
