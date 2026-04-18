package com.chaosbuffalo.mkcore.abilities2.runtime;

public final class NoopAbilityStateStore implements AbilityStateStore {
    public static final NoopAbilityStateStore INSTANCE = new NoopAbilityStateStore();

    private NoopAbilityStateStore() {
    }
}
