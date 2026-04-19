package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import com.chaosbuffalo.mkcore.core.IMKEntityData;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public record InternalActivationRequest(
        IMKEntityData ownerData,
        IMKEntityData casterData,
        AbilityReference ability,
        String activationId,
        @Nullable UUID sourceId,
        @Nullable AbilityResolvedTargets forcedTargets,
        @Nullable AbilityEventSnapshot eventSnapshot,
        boolean ignoreCosts,
        boolean ignoreCooldowns,
        ActivationReason reason,
        int chainDepth,
        @Nullable UUID parentInvocationId,
        @Nullable UUID inheritedRootInvocationId,
        @Nullable String entryPointOverride,
        @Nullable AbilityTargetResolverDefinition targetingOverride,
        @Nullable AbilityReactionOwner reactionOwner,
        boolean clearReactionOwnerOnCompletion,
        boolean clearReactionOwnerOnInterruption
) {
    public InternalActivationRequest {
        Objects.requireNonNull(ownerData, "ownerData");
        Objects.requireNonNull(casterData, "casterData");
        Objects.requireNonNull(ability, "ability");
        if (activationId == null || activationId.isBlank()) {
            throw new IllegalArgumentException("Internal activation request activationId must not be blank");
        }
        Objects.requireNonNull(reason, "reason");
        if (chainDepth < 0) {
            throw new IllegalArgumentException("Internal activation request chainDepth must be >= 0");
        }
        if (entryPointOverride != null && entryPointOverride.isBlank()) {
            throw new IllegalArgumentException("Internal activation request entryPointOverride must not be blank");
        }
    }
}
