package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AbilityInvocation {
    private final UUID invocationId;
    private final UUID rootInvocationId;
    private final int chainDepth;
    private final @Nullable UUID parentInvocationId;
    private final ResourceLocation abilityId;
    private final @Nullable UUID abilityInstanceId;
    private final String activationId;
    private final String entryPointId;
    private final ActivationReason reason;
    private final IMKEntityData ownerData;
    private final IMKEntityData casterData;
    private final UUID sourceId;
    private final AbilityResolvedTargets targets;
    private final @Nullable AbilityEventSnapshot eventSnapshot;
    private final PatchedAbilityDefinition definition;
    private final Map<String, AbilityValue> grantParameterOverrides;
    private final AbilityStatSnapshot invocationStats;
    private final Map<String, AbilityValue> graphVars;
    private final RandomSource rng;
    private boolean hasProducedGameplayEffect;

    public AbilityInvocation(UUID invocationId,
                             UUID rootInvocationId,
                             int chainDepth,
                             @Nullable UUID parentInvocationId,
                             ResourceLocation abilityId,
                             @Nullable UUID abilityInstanceId,
                             String activationId,
                             String entryPointId,
                             ActivationReason reason,
                             IMKEntityData ownerData,
                             IMKEntityData casterData,
                             UUID sourceId,
                             AbilityResolvedTargets targets,
                             @Nullable AbilityEventSnapshot eventSnapshot,
                             PatchedAbilityDefinition definition,
                             Map<String, AbilityValue> grantParameterOverrides,
                             AbilityStatSnapshot invocationStats,
                             Map<String, AbilityValue> graphVars,
                             RandomSource rng) {
        this.invocationId = Objects.requireNonNull(invocationId, "invocationId");
        this.rootInvocationId = Objects.requireNonNull(rootInvocationId, "rootInvocationId");
        if (chainDepth < 0) {
            throw new IllegalArgumentException("Ability invocation chainDepth must be >= 0");
        }
        this.chainDepth = chainDepth;
        this.parentInvocationId = parentInvocationId;
        this.abilityId = Objects.requireNonNull(abilityId, "abilityId");
        this.abilityInstanceId = abilityInstanceId;
        if (activationId == null || activationId.isBlank()) {
            throw new IllegalArgumentException("Ability invocation activationId must not be blank");
        }
        if (entryPointId == null || entryPointId.isBlank()) {
            throw new IllegalArgumentException("Ability invocation entryPointId must not be blank");
        }
        this.activationId = activationId;
        this.entryPointId = entryPointId;
        this.reason = Objects.requireNonNull(reason, "reason");
        this.ownerData = Objects.requireNonNull(ownerData, "ownerData");
        this.casterData = Objects.requireNonNull(casterData, "casterData");
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId");
        this.targets = Objects.requireNonNull(targets, "targets");
        this.eventSnapshot = eventSnapshot;
        this.definition = Objects.requireNonNull(definition, "definition");
        this.grantParameterOverrides = Collections.unmodifiableMap(new LinkedHashMap<>(
                Objects.requireNonNull(grantParameterOverrides, "grantParameterOverrides")));
        this.invocationStats = Objects.requireNonNull(invocationStats, "invocationStats");
        this.graphVars = new HashMap<>(Objects.requireNonNull(graphVars, "graphVars"));
        this.rng = Objects.requireNonNull(rng, "rng");
        this.hasProducedGameplayEffect = false;
    }

    public UUID invocationId() {
        return invocationId;
    }

    public UUID rootInvocationId() {
        return rootInvocationId;
    }

    public int chainDepth() {
        return chainDepth;
    }

    public @Nullable UUID parentInvocationId() {
        return parentInvocationId;
    }

    public ResourceLocation abilityId() {
        return abilityId;
    }

    public @Nullable UUID abilityInstanceId() {
        return abilityInstanceId;
    }

    public String activationId() {
        return activationId;
    }

    public String entryPointId() {
        return entryPointId;
    }

    public ActivationReason reason() {
        return reason;
    }

    public IMKEntityData ownerData() {
        return ownerData;
    }

    public IMKEntityData casterData() {
        return casterData;
    }

    public UUID sourceId() {
        return sourceId;
    }

    public AbilityResolvedTargets targets() {
        return targets;
    }

    public @Nullable AbilityEventSnapshot eventSnapshot() {
        return eventSnapshot;
    }

    public PatchedAbilityDefinition definition() {
        return definition;
    }

    public Map<String, AbilityValue> grantParameterOverrides() {
        return grantParameterOverrides;
    }

    public AbilityStatSnapshot invocationStats() {
        return invocationStats;
    }

    public Map<String, AbilityValue> graphVars() {
        return Collections.unmodifiableMap(graphVars);
    }

    public RandomSource rng() {
        return rng;
    }

    public boolean hasProducedGameplayEffect() {
        return hasProducedGameplayEffect;
    }

    public void markProducedGameplayEffect() {
        hasProducedGameplayEffect = true;
    }

    public @Nullable AbilityValue getGraphVar(String id) {
        return graphVars.get(id);
    }

    public void setGraphVar(String id, AbilityValue value) {
        graphVars.put(Objects.requireNonNull(id, "id"), Objects.requireNonNull(value, "value"));
    }
}
