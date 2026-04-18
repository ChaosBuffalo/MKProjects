package com.chaosbuffalo.mkcore.abilities2.definition;

import java.util.Objects;

public sealed interface AbilityPatchOperation permits AbilityPatchOperation.SetParameterPatchOperation,
        AbilityPatchOperation.ScaleParameterPatchOperation {

    String parameterId();

    record SetParameterPatchOperation(String parameterId,
                                      AbilityValue value) implements AbilityPatchOperation {
        public SetParameterPatchOperation {
            if (parameterId == null || parameterId.isBlank()) {
                throw new IllegalArgumentException("Set parameter patch operation parameterId must not be blank");
            }
            Objects.requireNonNull(value, "value");
        }
    }

    record ScaleParameterPatchOperation(String parameterId,
                                        double scale) implements AbilityPatchOperation {
        public ScaleParameterPatchOperation {
            if (parameterId == null || parameterId.isBlank()) {
                throw new IllegalArgumentException("Scale parameter patch operation parameterId must not be blank");
            }
        }
    }
}
