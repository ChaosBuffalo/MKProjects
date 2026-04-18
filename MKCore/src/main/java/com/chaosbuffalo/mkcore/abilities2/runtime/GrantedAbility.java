package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record GrantedAbility(
        UUID grantId,
        ResourceLocation abilityId,
        Map<String, AbilityValue> parameterOverrides,
        AbilityGrantSource source
) {
    public GrantedAbility {
        Objects.requireNonNull(grantId, "grantId");
        Objects.requireNonNull(abilityId, "abilityId");
        parameterOverrides = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(parameterOverrides, "parameterOverrides")));
        Objects.requireNonNull(source, "source");
    }
}
