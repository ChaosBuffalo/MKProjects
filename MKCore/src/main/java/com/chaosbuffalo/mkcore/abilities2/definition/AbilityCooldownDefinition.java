package com.chaosbuffalo.mkcore.abilities2.definition;

import java.util.Objects;

public record AbilityCooldownDefinition(
        StateScope scope,
        String key,
        AbilityScalar duration
) {
    public AbilityCooldownDefinition {
        Objects.requireNonNull(scope, "scope");
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Ability cooldown key must not be blank");
        }
        Objects.requireNonNull(duration, "duration");
    }
}
