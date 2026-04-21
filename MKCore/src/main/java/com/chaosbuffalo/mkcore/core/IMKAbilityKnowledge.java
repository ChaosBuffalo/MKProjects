package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

public interface IMKAbilityKnowledge {
    Collection<MKAbilityInfo> getAllAbilities();

    default Stream<ResourceLocation> getKnownAbilityIds() {
        return getAllAbilities().stream().map(MKAbilityInfo::getId);
    }

    boolean learnAbility(MKAbility ability, AbilitySource source);

    boolean unlearnAbility(ResourceLocation abilityId, AbilitySource source);

    boolean knowsAbility(ResourceLocation abilityId);

    @Nullable
    MKAbilityInfo getAbilityInfo(ResourceLocation abilityId);
}
