package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IMKAbilityProvider {
    @Deprecated
    @Nullable
    MKAbility getAbility(ItemStack item);

    default MKAbilityInfo getAbilityInfo(ItemStack itemStack) {
        MKAbility ability = getAbility(itemStack);
        return ability != null ? ability.getDefaultInstance() : null;
    }

}
