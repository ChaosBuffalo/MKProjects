package com.chaosbuffalo.mkcore.abilities2.datagen;

import com.chaosbuffalo.mkcore.abilities2.AbilityDefinitionService;
import com.chaosbuffalo.mkcore.abilities2.codec.AbilityCodecs;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityDefinitionData;
import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.JsonOps;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbilityDefinitionProvider extends MKDataProvider {
    private final Map<ResourceLocation, AbilityDefinitionData> definitions = new LinkedHashMap<>();

    public AbilityDefinitionProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Abilities2 Definitions");
    }

    protected abstract void addDefinitions();

    protected final void add(AbilityDefinitionData definition) {
        AbilityDefinitionData previous = definitions.put(definition.id(), definition);
        if (previous != null) {
            throw new IllegalStateException("Duplicate abilities2 definition id " + definition.id());
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        definitions.clear();
        addDefinitions();
        return CompletableFuture.allOf(definitions.values().stream()
                .map(definition -> writeDefinition(output, definition))
                .toArray(CompletableFuture[]::new));
    }

    protected CompletableFuture<?> writeDefinition(CachedOutput output, AbilityDefinitionData definition) {
        Path outputFolder = generator.getPackOutput().getOutputFolder();
        Path local = Paths.get("data", definition.id().getNamespace(), AbilityDefinitionService.ABILITY_DEFINITION_FOLDER,
                definition.id().getPath() + ".json");
        Path path = outputFolder.resolve(local);
        return DataProvider.saveStable(output,
                AbilityCodecs.ABILITY_DEFINITION_CODEC.encodeStart(JsonOps.INSTANCE, definition).getOrThrow(),
                path);
    }
}
