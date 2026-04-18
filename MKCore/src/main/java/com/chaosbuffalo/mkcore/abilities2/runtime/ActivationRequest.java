package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.core.IMKEntityData;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public record ActivationRequest(
        IMKEntityData ownerData,
        IMKEntityData casterData,
        AbilityReference ability,
        String activationId,
        @Nullable UUID sourceId,
        @Nullable AbilityResolvedTargets forcedTargets,
        @Nullable AbilityEventSnapshot eventSnapshot,
        boolean ignoreCosts,
        boolean ignoreCooldowns
) {
    public ActivationRequest {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(ability, "ability");
        if (activationId == null || activationId.isBlank()) {
            throw new IllegalArgumentException("Activation request activationId must not be blank");
        }
    }
}
