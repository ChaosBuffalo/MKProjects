package com.chaosbuffalo.mkcore.data;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public abstract class MKAbilityProvider extends MKDataProvider {

    public MKAbilityProvider(DataGenerator generator, String modId) {
        super(generator, modId, "MK Abilities");
    }


    public CompletableFuture<?> writeAbility(ResourceLocation key, MKAbility ability, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        String name = key.getPath().substring(8); // skip ability.
        Path local = Paths.get("data", key.getNamespace(), AbilityManager.DEFINITION_FOLDER, name + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = ability.serializeDatagen(JsonOps.INSTANCE);
        return DataProvider.saveStable(pOutput, element, path);
    }

    public static class FromMod extends MKAbilityProvider {

        public FromMod(DataGenerator generator, String modId) {
            super(generator, modId);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return CompletableFuture.allOf(
                    MKCoreRegistry.ABILITIES.getEntries().stream()
                            .filter(entry -> entry.getKey().location().getNamespace().equals(getModId()))
                            .map(entry -> writeAbility(entry.getKey().location(), entry.getValue(), pOutput))
                            .toList().toArray(CompletableFuture[]::new));
        }
    }
}

