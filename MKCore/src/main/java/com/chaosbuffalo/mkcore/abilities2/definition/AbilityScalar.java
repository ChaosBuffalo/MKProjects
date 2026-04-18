package com.chaosbuffalo.mkcore.abilities2.definition;

import com.chaosbuffalo.mkcore.abilities2.runtime.StatCapturePolicy;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public sealed interface AbilityScalar permits AbilityScalar.ConstantScalar, AbilityScalar.ParameterScalar,
        AbilityScalar.AttributeScaledScalar {

    record ConstantScalar(double value) implements AbilityScalar {
    }

    record ParameterScalar(String parameter) implements AbilityScalar {
        public ParameterScalar {
            if (parameter == null || parameter.isBlank()) {
                throw new IllegalArgumentException("Ability scalar parameter must not be blank");
            }
        }
    }

    record AttributeScaledScalar(AbilityScalar base,
                                 AbilityScalar scale,
                                 ResourceLocation attribute,
                                 StatCapturePolicy capturePolicy) implements AbilityScalar {
        public AttributeScaledScalar {
            Objects.requireNonNull(base, "base");
            Objects.requireNonNull(scale, "scale");
            Objects.requireNonNull(attribute, "attribute");
            Objects.requireNonNull(capturePolicy, "capturePolicy");
        }
    }
}
