package com.chaosbuffalo.mkcore.abilities2.definition;

import com.chaosbuffalo.mkcore.abilities2.actions.AbilityAction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record AbilityDeliveryDefinition(
        DeliveryKind kind,
        @Nullable ResourceLocation entityType,
        @Nullable ResourceLocation renderItem,
        List<AbilityAction> onSpawn,
        @Nullable AbilityScalar delayTicks,
        @Nullable AbilityScalar durationTicks,
        @Nullable AbilityScalar tickIntervalTicks,
        @Nullable AbilityScalar radius,
        @Nullable String onImpactActivationId,
        @Nullable String onAirTickActivationId,
        @Nullable String onGroundTickActivationId
) {
    public AbilityDeliveryDefinition {
        Objects.requireNonNull(kind, "kind");
        onSpawn = List.copyOf(Objects.requireNonNull(onSpawn, "onSpawn"));
        if (kind == DeliveryKind.PROJECTILE && entityType == null) {
            throw new IllegalArgumentException("Projectile deliveries require an entityType");
        }
        if (kind != DeliveryKind.PROJECTILE && entityType != null) {
            throw new IllegalArgumentException("Only projectile deliveries support an entityType");
        }
    }
}
