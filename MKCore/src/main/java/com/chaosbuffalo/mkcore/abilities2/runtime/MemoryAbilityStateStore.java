package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        return getRemainingTicks(cooldownExpiry, cooldownKey, gameTick, false);
    }

    @Override
    public void setCooldown(AbilityInvocation invocation, StateScope scope, String key, int durationTicks, long gameTick) {
        CooldownKey cooldownKey = new CooldownKey(resolveScopeKey(invocation, scope), key);
        setExpiry(cooldownExpiry, cooldownKey, durationTicks, gameTick);
    }

    @Override
    public int getGcdRemainingTicks(IMKEntityData ownerData, ResourceLocation gcdGroup, long gameTick) {
        GcdKey gcdKey = new GcdKey(ownerData.getEntity().getUUID(), gcdGroup);
        return getRemainingTicks(gcdExpiry, gcdKey, gameTick, true);
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

    @Override
    public void tick(long gameTick, CooldownFinishedListener listener) {
        Objects.requireNonNull(listener, "listener");
        Iterator<Map.Entry<CooldownKey, Long>> cooldownIterator = cooldownExpiry.entrySet().iterator();
        while (cooldownIterator.hasNext()) {
            Map.Entry<CooldownKey, Long> entry = cooldownIterator.next();
            if (entry.getValue() > gameTick) {
                continue;
            }
            cooldownIterator.remove();
            ScopeKey scopeKey = entry.getKey().scopeKey();
            listener.onCooldownFinished(new CooldownFinishedEvent(
                    scopeKey.scope(),
                    entry.getKey().key(),
                    scopeKey.abilityId(),
                    scopeKey.stableSourceId(),
                    scopeKey.ownerEntityId()
            ));
        }

        pruneExpired(gcdExpiry, gameTick);
    }

    public PersistedAbilityRuntimeState snapshotOwner(UUID ownerEntityId, long gameTick) {
        Objects.requireNonNull(ownerEntityId, "ownerEntityId");

        List<PersistedAbilityRuntimeState.CooldownEntry> cooldowns = cooldownExpiry.entrySet().stream()
                .filter(entry -> entry.getKey().scopeKey().ownerEntityId().equals(ownerEntityId))
                .map(entry -> snapshotCooldown(entry, gameTick))
                .filter(Objects::nonNull)
                .toList();

        List<PersistedAbilityRuntimeState.GcdEntry> gcds = gcdExpiry.entrySet().stream()
                .filter(entry -> entry.getKey().ownerEntityId().equals(ownerEntityId))
                .map(entry -> snapshotGcd(entry, gameTick))
                .filter(Objects::nonNull)
                .toList();

        List<PersistedAbilityRuntimeState.StateEntry> states = stateValues.entrySet().stream()
                .filter(entry -> entry.getKey().scopeKey().ownerEntityId().equals(ownerEntityId))
                .map(this::snapshotState)
                .toList();

        return new PersistedAbilityRuntimeState(cooldowns, gcds, states, List.of(), List.of());
    }

    public void restoreOwner(UUID ownerEntityId, PersistedAbilityRuntimeState snapshot, long gameTick) {
        Objects.requireNonNull(ownerEntityId, "ownerEntityId");
        Objects.requireNonNull(snapshot, "snapshot");

        clearOwner(ownerEntityId);

        for (PersistedAbilityRuntimeState.CooldownEntry entry : snapshot.cooldowns()) {
            if (entry.remainingTicks() <= 0) {
                continue;
            }
            cooldownExpiry.put(new CooldownKey(
                            new ScopeKey(entry.scope(), ownerEntityId, entry.stableSourceId(), entry.abilityId()),
                            entry.key()),
                    gameTick + entry.remainingTicks());
        }

        for (PersistedAbilityRuntimeState.GcdEntry entry : snapshot.gcds()) {
            if (entry.remainingTicks() <= 0) {
                continue;
            }
            gcdExpiry.put(new GcdKey(ownerEntityId, entry.gcdGroup()), gameTick + entry.remainingTicks());
        }

        for (PersistedAbilityRuntimeState.StateEntry entry : snapshot.states()) {
            stateValues.put(new StateKey(
                    new ScopeKey(entry.scope(), ownerEntityId, entry.stableSourceId(), entry.abilityId()),
                    entry.stateKey()
            ), entry.value());
        }
    }

    public void clearOwner(UUID ownerEntityId) {
        Objects.requireNonNull(ownerEntityId, "ownerEntityId");
        cooldownExpiry.entrySet().removeIf(entry -> entry.getKey().scopeKey().ownerEntityId().equals(ownerEntityId));
        gcdExpiry.entrySet().removeIf(entry -> entry.getKey().ownerEntityId().equals(ownerEntityId));
        stateValues.entrySet().removeIf(entry -> entry.getKey().scopeKey().ownerEntityId().equals(ownerEntityId));
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

    private @Nullable PersistedAbilityRuntimeState.CooldownEntry snapshotCooldown(Map.Entry<CooldownKey, Long> entry,
                                                                                  long gameTick) {
        int remainingTicks = remainingTicks(entry.getValue(), gameTick);
        if (remainingTicks <= 0) {
            return null;
        }
        ScopeKey scopeKey = entry.getKey().scopeKey();
        return new PersistedAbilityRuntimeState.CooldownEntry(
                scopeKey.scope(),
                entry.getKey().key(),
                remainingTicks,
                scopeKey.stableSourceId(),
                scopeKey.abilityId()
        );
    }

    private @Nullable PersistedAbilityRuntimeState.GcdEntry snapshotGcd(Map.Entry<GcdKey, Long> entry, long gameTick) {
        int remainingTicks = remainingTicks(entry.getValue(), gameTick);
        return remainingTicks > 0
                ? new PersistedAbilityRuntimeState.GcdEntry(entry.getKey().gcdGroup(), remainingTicks)
                : null;
    }

    private PersistedAbilityRuntimeState.StateEntry snapshotState(Map.Entry<StateKey, AbilityValue> entry) {
        ScopeKey scopeKey = entry.getKey().scopeKey();
        return new PersistedAbilityRuntimeState.StateEntry(
                scopeKey.scope(),
                entry.getKey().stateKey(),
                scopeKey.stableSourceId(),
                scopeKey.abilityId(),
                entry.getValue()
        );
    }

    private <K> int getRemainingTicks(Map<K, Long> expiryMap, K key, long gameTick, boolean removeExpired) {
        Long expiry = expiryMap.get(key);
        if (expiry == null) {
            return 0;
        }
        if (expiry <= gameTick) {
            if (removeExpired) {
                expiryMap.remove(key);
            }
            return 0;
        }
        long remaining = expiry - gameTick;
        return remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    private int remainingTicks(long expiry, long gameTick) {
        if (expiry <= gameTick) {
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

    private <K> void pruneExpired(Map<K, Long> expiryMap, long gameTick) {
        Iterator<Map.Entry<K, Long>> iterator = expiryMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= gameTick) {
                iterator.remove();
            }
        }
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
