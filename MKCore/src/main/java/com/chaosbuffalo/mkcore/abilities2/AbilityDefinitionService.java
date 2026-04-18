package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.codec.AbilityCodecs;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionPatch;
import com.chaosbuffalo.mkcore.abilities2.runtime.AbilityDefinitionResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nullable;
import java.util.*;

public class AbilityDefinitionService {
    public static final String ABILITY_DEFINITION_FOLDER = "mkcore/ability_definitions";
    public static final String ABILITY_DEFINITION_PATCH_FOLDER = "mkcore/ability_definition_patches";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, AbilityDefinitionData> definitions = new HashMap<>();
    private final Map<ResourceLocation, AbilityDefinitionPatch> patches = new HashMap<>();
    private final Map<ResourceLocation, List<AbilityDefinitionPatch>> patchesByAbility = new HashMap<>();
    private final AbilityDefinitionResolver resolver;
    private final DefinitionReloadListener definitionReloadListener;
    private final PatchReloadListener patchReloadListener;

    public AbilityDefinitionService() {
        this.resolver = new AbilityDefinitionResolver(this::getDefinition, this::getPatches);
        this.definitionReloadListener = new DefinitionReloadListener();
        this.patchReloadListener = new PatchReloadListener();
    }

    public DefinitionReloadListener getDefinitionReloadListener() {
        return definitionReloadListener;
    }

    public PatchReloadListener getPatchReloadListener() {
        return patchReloadListener;
    }

    public AbilityDefinitionResolver getResolver() {
        return resolver;
    }

    @Nullable
    public AbilityDefinitionData getDefinition(ResourceLocation abilityId) {
        return definitions.get(abilityId);
    }

    public List<AbilityDefinitionPatch> getPatches(ResourceLocation abilityId) {
        return patchesByAbility.getOrDefault(abilityId, List.of());
    }

    private void onDefinitionsReload(Map<ResourceLocation, JsonElement> objects) {
        definitions.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                AbilityDefinitionData definition = parseDefinition(entry.getKey(), entry.getValue());
                definitions.put(definition.id(), definition);
            } catch (Exception e) {
                MKCore.LOGGER.error("Failed to load abilities2 definition {}", entry.getKey(), e);
            }
        }
        resolver.clearCaches();
        MKCore.LOGGER.info("Loaded {} abilities2 definitions", definitions.size());
    }

    private void onPatchesReload(Map<ResourceLocation, JsonElement> objects) {
        patches.clear();
        patchesByAbility.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                AbilityDefinitionPatch patch = parsePatch(entry.getKey(), entry.getValue());
                patches.put(patch.patchId(), patch);
                patchesByAbility.computeIfAbsent(patch.abilityId(), ignored -> new ArrayList<>()).add(patch);
            } catch (Exception e) {
                MKCore.LOGGER.error("Failed to load abilities2 patch {}", entry.getKey(), e);
            }
        }
        patchesByAbility.replaceAll((abilityId, list) -> List.copyOf(list));
        resolver.clearCaches();
        MKCore.LOGGER.info("Loaded {} abilities2 definition patches", patches.size());
    }

    private AbilityDefinitionData parseDefinition(ResourceLocation fileId, JsonElement rawJson) {
        JsonObject object = requireObject(fileId, rawJson, "ability definition");
        JsonObject prepared = object.deepCopy();
        if (!prepared.has("id")) {
            prepared.addProperty("id", fileId.toString());
        }
        AbilityDefinitionData definition = AbilityCodecs.ABILITY_DEFINITION_CODEC.parse(JsonOps.INSTANCE, prepared).getOrThrow();
        if (!definition.id().equals(fileId)) {
            throw new IllegalArgumentException("Ability definition id %s does not match file id %s"
                    .formatted(definition.id(), fileId));
        }
        return definition;
    }

    private AbilityDefinitionPatch parsePatch(ResourceLocation fileId, JsonElement rawJson) {
        JsonObject object = requireObject(fileId, rawJson, "ability definition patch");
        JsonObject prepared = object.deepCopy();
        if (!prepared.has("patch_id")) {
            prepared.addProperty("patch_id", fileId.toString());
        }
        AbilityDefinitionPatch patch = AbilityCodecs.ABILITY_DEFINITION_PATCH_CODEC.parse(JsonOps.INSTANCE, prepared).getOrThrow();
        if (!patch.patchId().equals(fileId)) {
            throw new IllegalArgumentException("Ability definition patch id %s does not match file id %s"
                    .formatted(patch.patchId(), fileId));
        }
        return patch;
    }

    private JsonObject requireObject(ResourceLocation fileId, JsonElement rawJson, String label) {
        if (!rawJson.isJsonObject()) {
            throw new IllegalArgumentException("Expected %s %s to be a JSON object".formatted(label, fileId));
        }
        return rawJson.getAsJsonObject();
    }

    public class DefinitionReloadListener extends SimpleJsonResourceReloadListener {
        public DefinitionReloadListener() {
            super(GSON, ABILITY_DEFINITION_FOLDER);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
            onDefinitionsReload(objectIn);
        }
    }

    public class PatchReloadListener extends SimpleJsonResourceReloadListener {
        public PatchReloadListener() {
            super(GSON, ABILITY_DEFINITION_PATCH_FOLDER);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
            onPatchesReload(objectIn);
        }
    }
}
