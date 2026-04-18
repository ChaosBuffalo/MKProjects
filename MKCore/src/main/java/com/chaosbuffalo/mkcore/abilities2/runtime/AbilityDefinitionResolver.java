package com.chaosbuffalo.mkcore.abilities2.runtime;

import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.CompiledAbilityDefinition;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class AbilityDefinitionResolver {
    private final Function<ResourceLocation, AbilityDefinitionData> definitionLookup;
    private final Map<ResourceLocation, Optional<CompiledAbilityDefinition>> compiledCache = new HashMap<>();
    private final Map<ResourceLocation, Optional<PatchedAbilityDefinition>> patchedCache = new HashMap<>();

    public AbilityDefinitionResolver(Function<ResourceLocation, AbilityDefinitionData> definitionLookup) {
        this.definitionLookup = Objects.requireNonNull(definitionLookup, "definitionLookup");
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
        return Optional.of(new PatchedAbilityDefinition(compiled, compiled.createDefaultParameterMap()));
    }
}
