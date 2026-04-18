package com.chaosbuffalo.mkcore.abilities2.definition;

public record AbilityTargetResolverDefinition(String type) {
    public AbilityTargetResolverDefinition {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Ability target resolver type must not be blank");
        }
    }
}
