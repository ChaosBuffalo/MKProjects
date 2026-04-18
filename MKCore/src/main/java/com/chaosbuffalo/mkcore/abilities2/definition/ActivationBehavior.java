package com.chaosbuffalo.mkcore.abilities2.definition;

import javax.annotation.Nullable;

public sealed interface ActivationBehavior permits ActivationBehavior.InstantBehavior,
        ActivationBehavior.ChannelBehavior, ActivationBehavior.AuraBehavior {

    record InstantBehavior() implements ActivationBehavior {
    }

    record ChannelBehavior(int tickIntervalTicks,
                           String tickEntryPoint,
                           boolean preserveInitialTargets,
                           @Nullable AbilityTargetResolverDefinition tickTargeting) implements ActivationBehavior {
        public ChannelBehavior {
            if (tickIntervalTicks <= 0) {
                throw new IllegalArgumentException("Channel tickIntervalTicks must be > 0");
            }
            if (tickEntryPoint == null || tickEntryPoint.isBlank()) {
                throw new IllegalArgumentException("Channel tickEntryPoint must not be blank");
            }
            if (preserveInitialTargets && tickTargeting != null) {
                throw new IllegalArgumentException("Channel behavior with preserveInitialTargets must not declare tickTargeting");
            }
        }
    }

    record AuraBehavior(int pulseIntervalTicks,
                        String pulseEntryPoint,
                        AbilityTargetResolverDefinition pulseTargeting,
                        boolean pulseOnEnable) implements ActivationBehavior {
        public AuraBehavior {
            if (pulseIntervalTicks <= 0) {
                throw new IllegalArgumentException("Aura pulseIntervalTicks must be > 0");
            }
            if (pulseEntryPoint == null || pulseEntryPoint.isBlank()) {
                throw new IllegalArgumentException("Aura pulseEntryPoint must not be blank");
            }
            if (pulseTargeting == null) {
                throw new IllegalArgumentException("Aura pulseTargeting must not be null");
            }
        }
    }
}
