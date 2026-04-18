package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionPatch;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityParameterDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityPatchOperation;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValueKind;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.CompiledAbilityDefinition;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue.FloatValue;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityValue.IntValue;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class AbilityDefinitionResolver {
    private final Function<ResourceLocation, AbilityDefinitionData> definitionLookup;
    private final Function<ResourceLocation, List<AbilityDefinitionPatch>> patchLookup;
    private final Map<ResourceLocation, Optional<CompiledAbilityDefinition>> compiledCache = new HashMap<>();
    private final Map<ResourceLocation, Optional<PatchedAbilityDefinition>> patchedCache = new HashMap<>();
    private static final Comparator<AbilityDefinitionPatch> PATCH_ORDER = Comparator
            .comparingInt(AbilityDefinitionPatch::priority)
            .thenComparingInt(AbilityDefinitionPatch::loadOrder)
            .thenComparing(patch -> patch.patchId().toString());

    public AbilityDefinitionResolver(Function<ResourceLocation, AbilityDefinitionData> definitionLookup) {
        this(definitionLookup, abilityId -> List.of());
    }

    public AbilityDefinitionResolver(Function<ResourceLocation, AbilityDefinitionData> definitionLookup,
                                     Function<ResourceLocation, List<AbilityDefinitionPatch>> patchLookup) {
        this.definitionLookup = Objects.requireNonNull(definitionLookup, "definitionLookup");
        this.patchLookup = Objects.requireNonNull(patchLookup, "patchLookup");
    }

    @Nullable
    public CompiledAbilityDefinition resolveCompiled(ResourceLocation abilityId) {
        return compiledCache.computeIfAbsent(abilityId, this::compileDefinition).orElse(null);
    }

    @Nullable
    public PatchedAbilityDefinition resolvePatched(ResourceLocation abilityId) {
        return patchedCache.computeIfAbsent(abilityId, this::resolvePatchedDefinition).orElse(null);
    }

    public void clearCaches() {
        compiledCache.clear();
        patchedCache.clear();
    }

    private Optional<CompiledAbilityDefinition> compileDefinition(ResourceLocation abilityId) {
        AbilityDefinitionData data = definitionLookup.apply(Objects.requireNonNull(abilityId, "abilityId"));
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(CompiledAbilityDefinition.compile(data));
    }

    private Optional<PatchedAbilityDefinition> resolvePatchedDefinition(ResourceLocation abilityId) {
        CompiledAbilityDefinition compiled = resolveCompiled(abilityId);
        if (compiled == null) {
            return Optional.empty();
        }
        Map<String, AbilityValue> patchedParameters = applyPatches(compiled, abilityId);
        return Optional.of(new PatchedAbilityDefinition(compiled, patchedParameters));
    }

    private Map<String, AbilityValue> applyPatches(CompiledAbilityDefinition compiled, ResourceLocation abilityId) {
        LinkedHashMap<String, AbilityValue> resolved = new LinkedHashMap<>(compiled.createDefaultParameterMap());
        List<AbilityDefinitionPatch> patches = patchLookup.apply(abilityId);
        if (patches == null || patches.isEmpty()) {
            return resolved;
        }

        List<AbilityDefinitionPatch> ordered = new ArrayList<>(patches);
        ordered.sort(PATCH_ORDER);
        for (AbilityDefinitionPatch patch : ordered) {
            validatePatchTarget(compiled, patch);
            for (AbilityPatchOperation operation : patch.operations()) {
                applyOperation(compiled, patch, resolved, operation);
            }
        }
        return resolved;
    }

    private void validatePatchTarget(CompiledAbilityDefinition compiled, AbilityDefinitionPatch patch) {
        if (!compiled.data().id().equals(patch.abilityId())) {
            throw new IllegalArgumentException("Patch %s targets %s but was resolved for %s"
                    .formatted(patch.patchId(), patch.abilityId(), compiled.data().id()));
        }
    }

    private void applyOperation(CompiledAbilityDefinition compiled,
                                AbilityDefinitionPatch patch,
                                Map<String, AbilityValue> resolved,
                                AbilityPatchOperation operation) {
        AbilityParameterDefinition parameter = compiled.data().parameters().get(operation.parameterId());
        if (parameter == null) {
            throw new IllegalArgumentException("Patch %s references unknown parameter '%s' on ability %s"
                    .formatted(patch.patchId(), operation.parameterId(), compiled.data().id()));
        }
        if (!parameter.patchable()) {
            throw new IllegalArgumentException("Patch %s attempted to modify non-patchable parameter '%s' on ability %s"
                    .formatted(patch.patchId(), operation.parameterId(), compiled.data().id()));
        }

        switch (operation) {
            case AbilityPatchOperation.SetParameterPatchOperation setOp -> {
                if (setOp.value().kind() != parameter.kind()) {
                    throw new IllegalArgumentException("Patch %s parameter '%s' value kind %s does not match expected %s"
                            .formatted(patch.patchId(), parameter.id(), setOp.value().kind(), parameter.kind()));
                }
                resolved.put(parameter.id(), setOp.value());
            }
            case AbilityPatchOperation.ScaleParameterPatchOperation scaleOp -> {
                if (!parameter.kind().isNumeric()) {
                    throw new IllegalArgumentException("Patch %s attempted to scale non-numeric parameter '%s' of kind %s"
                            .formatted(patch.patchId(), parameter.id(), parameter.kind()));
                }
                AbilityValue current = resolved.get(parameter.id());
                resolved.put(parameter.id(), scaleNumericValue(current, scaleOp.scale(), parameter.kind()));
            }
        }
    }

    private AbilityValue scaleNumericValue(AbilityValue current, double scale, AbilityValueKind kind) {
        return switch (kind) {
            case FLOAT -> new FloatValue(current.asFloat("scaled") * (float) scale);
            case INT -> new IntValue((int) Math.round(current.asInt("scaled") * scale));
            case BOOL, ENTITY_REF, RESOURCE_LOCATION ->
                    throw new IllegalStateException("Non-numeric kind %s reached numeric scaling".formatted(kind));
        };
    }
}
