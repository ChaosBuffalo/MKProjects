package com.chaosbuffalo.mkcore.abilities2.definition;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityEventFilter;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityEventType;

import java.util.List;
import java.util.Objects;

public record AbilityReactionDefinition(
        AbilityEventType eventType,
        List<AbilityEventFilter> filters,
        float chance,
        int internalCooldownTicks,
        boolean oncePerRoot,
        int maxChainDepth,
        String activationId
) {
    public AbilityReactionDefinition {
        Objects.requireNonNull(eventType, "eventType");
        filters = List.copyOf(Objects.requireNonNull(filters, "filters"));
        if (chance < 0.0f || chance > 1.0f) {
            throw new IllegalArgumentException("Ability reaction chance must be between 0 and 1");
        }
        if (internalCooldownTicks < 0) {
            throw new IllegalArgumentException("Ability reaction internalCooldownTicks must be >= 0");
        }
        if (maxChainDepth < 0) {
            throw new IllegalArgumentException("Ability reaction maxChainDepth must be >= 0");
        }
        if (activationId == null || activationId.isBlank()) {
            throw new IllegalArgumentException("Ability reaction activationId must not be blank");
        }
    }
}
