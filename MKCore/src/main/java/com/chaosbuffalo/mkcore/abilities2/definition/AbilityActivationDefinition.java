package com.chaosbuffalo.mkcore.abilities2.definition;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record AbilityActivationDefinition(
        ActivationKind kind,
        String entryPoint,
        AbilityTargetResolverDefinition targeting,
        List<AbilityCostDefinition> costs,
        List<AbilityCooldownDefinition> cooldowns,
        @Nullable ResourceLocation gcdGroup,
        int castTicks,
        boolean affectedByCastSpeed,
        InterruptPolicy interruptPolicy,
        InterruptRefundPolicy refundPolicy,
        ActivationBehavior behavior
) {
    public AbilityActivationDefinition {
        Objects.requireNonNull(kind, "kind");
        if (entryPoint == null || entryPoint.isBlank()) {
            throw new IllegalArgumentException("Ability activation entryPoint must not be blank");
        }
        Objects.requireNonNull(targeting, "targeting");
        costs = List.copyOf(Objects.requireNonNull(costs, "costs"));
        cooldowns = List.copyOf(Objects.requireNonNull(cooldowns, "cooldowns"));
        if (castTicks < 0) {
            throw new IllegalArgumentException("Ability activation castTicks must be >= 0");
        }
        Objects.requireNonNull(interruptPolicy, "interruptPolicy");
        Objects.requireNonNull(refundPolicy, "refundPolicy");
        Objects.requireNonNull(behavior, "behavior");
    }
}
