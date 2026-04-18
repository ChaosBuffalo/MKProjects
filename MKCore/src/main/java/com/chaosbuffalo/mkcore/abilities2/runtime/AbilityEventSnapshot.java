package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record AbilityEventSnapshot(
        AbilityEventType eventType,
        @Nullable UUID invocationId,
        @Nullable UUID rootInvocationId,
        int chainDepth,
        @Nullable UUID sourceId,
        @Nullable ResourceLocation sourceAbilityId,
        @Nullable String sourceActivationId,
        @Nullable UUID actorEntityId,
        @Nullable UUID targetEntityId,
        Map<String, AbilityValue> payload
) {
    public AbilityEventSnapshot {
        Objects.requireNonNull(eventType, "eventType");
        if (chainDepth < 0) {
            throw new IllegalArgumentException("Ability event chainDepth must be >= 0");
        }
        payload = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(payload, "payload")));
    }
}
