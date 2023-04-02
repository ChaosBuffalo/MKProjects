package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IMKAbilityKnowledge {
    Collection<MKAbilityInfo> getAllAbilities();

    boolean learnAbility(MKAbility ability, AbilitySource source);

    boolean unlearnAbility(ResourceLocation abilityId, AbilitySource source);

    boolean knowsAbility(ResourceLocation abilityId);

    @Nullable
    MKAbilityInfo getKnownAbility(ResourceLocation abilityId);
}
