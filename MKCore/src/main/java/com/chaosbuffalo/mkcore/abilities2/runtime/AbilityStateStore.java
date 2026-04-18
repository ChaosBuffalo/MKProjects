package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.StateScope;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public interface AbilityStateStore {
    record CooldownFinishedEvent(
            StateScope scope,
            String key,
            @Nullable ResourceLocation abilityId,
            @Nullable UUID stableSourceId,
            UUID ownerEntityId
    ) {
    }

    @FunctionalInterface
    interface CooldownFinishedListener {
        void onCooldownFinished(CooldownFinishedEvent event);
    }

    int getCooldownRemainingTicks(AbilityInvocation invocation, StateScope scope, String key, long gameTick);

    void setCooldown(AbilityInvocation invocation, StateScope scope, String key, int durationTicks, long gameTick);

    int getGcdRemainingTicks(IMKEntityData ownerData, ResourceLocation gcdGroup, long gameTick);

    void setGcd(IMKEntityData ownerData, ResourceLocation gcdGroup, int durationTicks, long gameTick);

    @Nullable
    AbilityValue getState(AbilityInvocation invocation, StateScope scope, String stateKey);

    void setState(AbilityInvocation invocation, StateScope scope, String stateKey, @Nullable AbilityValue value);

    default void tick(long gameTick, CooldownFinishedListener listener) {
    }
}
