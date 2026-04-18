package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public final class NoopAbilityStateStore implements AbilityStateStore {
    public static final NoopAbilityStateStore INSTANCE = new NoopAbilityStateStore();

    private NoopAbilityStateStore() {
    }

    @Override
    public int getCooldownRemainingTicks(AbilityInvocation invocation, StateScope scope, String key, long gameTick) {
        return 0;
    }

    @Override
    public void setCooldown(AbilityInvocation invocation, StateScope scope, String key, int durationTicks, long gameTick) {
    }

    @Override
    public int getGcdRemainingTicks(IMKEntityData ownerData, ResourceLocation gcdGroup, long gameTick) {
        return 0;
    }

    @Override
    public void setGcd(IMKEntityData ownerData, ResourceLocation gcdGroup, int durationTicks, long gameTick) {
    }

    @Override
    public @Nullable AbilityValue getState(AbilityInvocation invocation, StateScope scope, String stateKey) {
        return null;
    }

    @Override
    public void setState(AbilityInvocation invocation, StateScope scope, String stateKey, @Nullable AbilityValue value) {
    }
}
