package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.AbilityType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;


public class MKAbilityInfo {
    private final MKAbility ability;

    public MKAbilityInfo(MKAbility ability) {
        this.ability = ability;
    }

    @Nonnull
    public MKAbility getAbility() {
        return ability;
    }

    public AbilityType getAbilityType() {
        return ability.getType();
    }

    public ResourceLocation getId() {
        return ability.getAbilityId();
    }

    @Override
    public String toString() {
        return "MKAbilityInfo{" +
                "ability=" + ability + '}';
    }
}
