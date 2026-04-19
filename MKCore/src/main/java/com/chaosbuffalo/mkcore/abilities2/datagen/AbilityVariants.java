package com.chaosbuffalo.mkcore.abilities2.datagen;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityParameterDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityPresentation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public final class AbilityVariants {
    private AbilityVariants() {
    }

    public static AbilityDefinitionData variant(AbilityDefinitionData base,
                                                ResourceLocation newId,
                                                @Nullable AbilityPresentation newPresentation,
                                                Map<String, AbilityValue> parameterOverrides) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(newId, "newId");
        Objects.requireNonNull(parameterOverrides, "parameterOverrides");

        AbilityDefinitionBuilder builder = AbilityDefinitionBuilder.from(base)
                .id(newId)
                .presentation(newPresentation != null ? newPresentation : base.presentation());

        parameterOverrides.forEach((parameterId, overrideValue) -> {
            AbilityParameterDefinition parameter = base.parameters().get(parameterId);
            if (parameter == null) {
                throw new IllegalArgumentException("Unknown parameter '%s' for variant %s"
                        .formatted(parameterId, base.id()));
            }
            if (overrideValue.kind() != parameter.kind()) {
                throw new IllegalArgumentException("Parameter '%s' variant override kind %s does not match %s"
                        .formatted(parameterId, overrideValue.kind(), parameter.kind()));
            }
            builder.parameter(new AbilityParameterDefinition(
                    parameter.id(),
                    overrideValue,
                    parameter.kind(),
                    parameter.patchable(),
                    parameter.grantOverrideable(),
                    parameter.description()
            ));
        });

        return builder.build();
    }
}
