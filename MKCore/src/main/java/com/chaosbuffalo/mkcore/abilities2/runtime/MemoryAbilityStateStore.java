package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MemoryAbilityStateStore implements AbilityStateStore {
    private final Map<CooldownKey, Long> cooldownExpiry = new HashMap<>();
    private final Map<GcdKey, Long> gcdExpiry = new HashMap<>();
    private final Map<StateKey, AbilityValue> stateValues = new HashMap<>();

    @Override
    public int getCooldownRemainingTicks(AbilityInvocation invocation, StateScope scope, String key, long gameTick) {
        CooldownKey cooldownKey = new CooldownKey(resolveScopeKey(invocation, scope), key);
        return getRemainingTicks(cooldownExpiry, cooldownKey, gameTick);
    }

    @Override
    public void setCooldown(AbilityInvocation invocation, StateScope scope, String key, int durationTicks, long gameTick) {
        CooldownKey cooldownKey = new CooldownKey(resolveScopeKey(invocation, scope), key);
        setExpiry(cooldownExpiry, cooldownKey, durationTicks, gameTick);
    }

    @Override
    public int getGcdRemainingTicks(IMKEntityData ownerData, ResourceLocation gcdGroup, long gameTick) {
        GcdKey gcdKey = new GcdKey(ownerData.getEntity().getUUID(), gcdGroup);
        return getRemainingTicks(gcdExpiry, gcdKey, gameTick);
    }

    @Override
    public void setGcd(IMKEntityData ownerData, ResourceLocation gcdGroup, int durationTicks, long gameTick) {
        GcdKey gcdKey = new GcdKey(ownerData.getEntity().getUUID(), gcdGroup);
        setExpiry(gcdExpiry, gcdKey, durationTicks, gameTick);
    }

    @Override
    public @Nullable AbilityValue getState(AbilityInvocation invocation, StateScope scope, String stateKey) {
        return stateValues.get(new StateKey(resolveScopeKey(invocation, scope), stateKey));
    }

    @Override
    public void setState(AbilityInvocation invocation, StateScope scope, String stateKey, @Nullable AbilityValue value) {
        StateKey key = new StateKey(resolveScopeKey(invocation, scope), stateKey);
        if (value == null) {
            stateValues.remove(key);
        } else {
            stateValues.put(key, value);
        }
    }

    private ScopeKey resolveScopeKey(AbilityInvocation invocation, StateScope scope) {
        UUID ownerEntityId = invocation.ownerData().getEntity().getUUID();
        return switch (scope) {
            case SELF -> new ScopeKey(scope, ownerEntityId, null, null);
            case LOADOUT_SLOT, SOURCE_ITEM, SOURCE_EFFECT ->
                    new ScopeKey(scope, ownerEntityId, invocation.sourceId(), null);
            case ABILITY_INSTANCE -> new ScopeKey(scope, ownerEntityId,
                    invocation.abilityInstanceId() != null ? invocation.abilityInstanceId() : invocation.sourceId(),
                    invocation.abilityId());
            case ABILITY_FAMILY -> new ScopeKey(scope, ownerEntityId, null, invocation.abilityId());
        };
    }

    private <K> int getRemainingTicks(Map<K, Long> expiryMap, K key, long gameTick) {
        Long expiry = expiryMap.get(key);
        if (expiry == null) {
            return 0;
        }
        if (expiry <= gameTick) {
            expiryMap.remove(key);
            return 0;
        }
        long remaining = expiry - gameTick;
        return remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    private <K> void setExpiry(Map<K, Long> expiryMap, K key, int durationTicks, long gameTick) {
        if (durationTicks <= 0) {
            expiryMap.remove(key);
            return;
        }
        expiryMap.put(key, gameTick + durationTicks);
    }

    private record ScopeKey(StateScope scope,
                            UUID ownerEntityId,
                            @Nullable UUID stableSourceId,
                            @Nullable ResourceLocation abilityId) {
        private ScopeKey {
            Objects.requireNonNull(scope, "scope");
            Objects.requireNonNull(ownerEntityId, "ownerEntityId");
        }
    }

    private record CooldownKey(ScopeKey scopeKey, String key) {
        private CooldownKey {
            Objects.requireNonNull(scopeKey, "scopeKey");
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Cooldown key must not be blank");
            }
        }
    }

    private record StateKey(ScopeKey scopeKey, String stateKey) {
        private StateKey {
            Objects.requireNonNull(scopeKey, "scopeKey");
            if (stateKey == null || stateKey.isBlank()) {
                throw new IllegalArgumentException("State key must not be blank");
            }
        }
    }

    private record GcdKey(UUID ownerEntityId, ResourceLocation gcdGroup) {
        private GcdKey {
            Objects.requireNonNull(ownerEntityId, "ownerEntityId");
            Objects.requireNonNull(gcdGroup, "gcdGroup");
        }
    }
}
