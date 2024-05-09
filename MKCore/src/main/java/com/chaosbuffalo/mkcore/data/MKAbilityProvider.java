package com.chaosbuffalo.mkcore.data;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.abilities.AbilityTranslations;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public abstract class MKAbilityProvider extends MKDataProvider {

    public MKAbilityProvider(DataGenerator generator, String modId) {
        super(generator, modId, "MK Abilities");
    }


    public CompletableFuture<?> writeAbility(ResourceLocation key, MKAbility ability, CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        String name = key.getPath();
        Path local = Paths.get("data", key.getNamespace(), AbilityManager.DEFINITION_FOLDER, name + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = ability.serializeDynamic(JsonOps.INSTANCE);
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

    public static abstract class AbilityLanguageProvider {
        private final LanguageProvider provider;

        public AbilityLanguageProvider(LanguageProvider provider) {
            this.provider = provider;
        }

        public Builder ability(RegistryObject<? extends MKAbility> abilitySupplier) {
            return new Builder(provider, abilitySupplier);
        }

        public static class Builder {
            private final LanguageProvider provider;
            private final RegistryObject<? extends MKAbility> ability;
            private final Map<String, String> customValues = new TreeMap<>();
            private String name;
            private String description;

            public Builder(LanguageProvider provider, RegistryObject<? extends MKAbility> supplier) {
                this.provider = provider;
                this.ability = supplier;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder description(String desc) {
                this.description = desc;
                return this;
            }

            public Builder custom(String key, String value) {
                customValues.put(key, value);
                return this;
            }

            public void build() {
                ResourceLocation abilityId = ability.getId();
                if (name != null) {
                    provider.add(AbilityTranslations.nameKey(abilityId), name);
                }
                if (description != null) {
                    provider.add(AbilityTranslations.descriptionKey(abilityId), description);
                }
                customValues.forEach((k, v) -> {
                    provider.add(AbilityTranslations.customKey(abilityId, k), v);
                });
            }
        }
    }
}

