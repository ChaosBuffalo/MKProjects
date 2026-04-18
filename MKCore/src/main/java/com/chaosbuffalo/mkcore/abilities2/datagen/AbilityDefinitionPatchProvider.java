package com.chaosbuffalo.mkcore.abilities2.datagen;

import com.chaosbuffalo.mkcore.abilities2.AbilityDefinitionService;
import com.chaosbuffalo.mkcore.abilities2.codec.AbilityCodecs;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionPatch;
import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbilityDefinitionPatchProvider extends MKDataProvider {
    private final Map<ResourceLocation, AbilityDefinitionPatch> patches = new LinkedHashMap<>();

    public AbilityDefinitionPatchProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Abilities2 Definition Patches");
    }

    protected abstract void addPatches();

    protected final void add(AbilityDefinitionPatch patch) {
        AbilityDefinitionPatch previous = patches.put(patch.patchId(), patch);
        if (previous != null) {
            throw new IllegalStateException("Duplicate abilities2 patch id " + patch.patchId());
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        patches.clear();
        addPatches();
        return CompletableFuture.allOf(patches.values().stream()
                .map(patch -> writePatch(output, patch))
                .toArray(CompletableFuture[]::new));
    }

    protected CompletableFuture<?> writePatch(CachedOutput output, AbilityDefinitionPatch patch) {
        Path outputFolder = generator.getPackOutput().getOutputFolder();
        Path local = Paths.get("data", patch.patchId().getNamespace(), AbilityDefinitionService.ABILITY_DEFINITION_PATCH_FOLDER,
                patch.patchId().getPath() + ".json");
        Path path = outputFolder.resolve(local);
        return DataProvider.saveStable(output,
                AbilityCodecs.ABILITY_DEFINITION_PATCH_CODEC.encodeStart(JsonOps.INSTANCE, patch).getOrThrow(),
                path);
    }
}
