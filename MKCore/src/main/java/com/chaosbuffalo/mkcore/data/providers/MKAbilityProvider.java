package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.abilities.AbilityTranslations;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    public CompletableFuture<?> writeVariant(ResourceLocation key, MKAbility baseAbility, AbilityVariantPatch patch,
                                             CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        String name = key.getPath();
        Path local = Paths.get("data", key.getNamespace(), AbilityManager.DEFINITION_FOLDER, name + ".json");
        Path path = outputFolder.resolve(local);
        JsonObject element = applyVariantPatch(baseAbility.serializeDynamic(JsonOps.INSTANCE).getAsJsonObject(), patch);
        return DataProvider.saveStable(pOutput, element, path);
    }

    public static JsonObject applyVariantPatch(JsonObject base, AbilityVariantPatch patch) {
        JsonObject output = base.deepCopy();
        if (patch.cooldown() != null) {
            output.addProperty("cooldown", patch.cooldown());
        }
        if (patch.manaCost() != null) {
            output.addProperty("manaCost", patch.manaCost());
        }
        if (patch.castTime() != null) {
            output.addProperty("castTime", patch.castTime());
        }

        JsonObject attributes = output.has("attributes") && output.get("attributes").isJsonObject()
                ? output.getAsJsonObject("attributes")
                : new JsonObject();
        if (!output.has("attributes")) {
            output.add("attributes", attributes);
        }

        patch.attributeReplacements().forEach((key, value) -> attributes.add(key, value.deepCopy()));
        patch.attributeMerges().forEach((key, value) -> mergeAttribute(attributes, key, value));
        return output;
    }

    private static void mergeAttribute(JsonObject attributes, String key, JsonObject patch) {
        JsonElement existing = attributes.get(key);
        if (existing != null && existing.isJsonObject()) {
            mergeJsonObjects(existing.getAsJsonObject(), patch);
        } else {
            attributes.add(key, patch.deepCopy());
        }
    }

    private static void mergeJsonObjects(JsonObject target, JsonObject patch) {
        patch.entrySet().forEach(entry -> {
            JsonElement existing = target.get(entry.getKey());
            JsonElement patchValue = entry.getValue();
            if (existing != null && existing.isJsonObject() && patchValue.isJsonObject()) {
                mergeJsonObjects(existing.getAsJsonObject(), patchValue.getAsJsonObject());
            } else {
                target.add(entry.getKey(), patchValue.deepCopy());
            }
        });
    }

    /**
     * Sparse patch payload used by {@link #writeVariant(ResourceLocation, MKAbility, AbilityVariantPatch, CachedOutput)}.
     * <p>
     * Top-level numeric fields use boxed types intentionally so the patch can distinguish between:
     * <ul>
     *     <li>{@code null}: do not override the base ability value</li>
     *     <li>a concrete number such as {@code 0} or {@code 0.0f}: explicitly replace the base ability value</li>
     * </ul>
     * Primitive {@code int}/{@code float} fields would lose that "unset" state and force a sentinel value.
     */
    public record AbilityVariantPatch(Integer cooldown, Float manaCost, Integer castTime,
                                      Map<String, JsonElement> attributeReplacements,
                                      Map<String, JsonObject> attributeMerges) {

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Integer cooldown;
            private Float manaCost;
            private Integer castTime;
            private final Map<String, JsonElement> attributeReplacements = new LinkedHashMap<>();
            private final Map<String, JsonObject> attributeMerges = new LinkedHashMap<>();

            /**
             * Replaces the base ability cooldown. The builder accepts a primitive for call-site ergonomics,
             * but stores it as a nullable {@link Integer} so the patch can still represent "no override".
             */
            public Builder cooldown(int cooldown) {
                this.cooldown = cooldown;
                return this;
            }

            /**
             * Replaces the base ability mana cost. {@code 0.0f} remains a valid explicit override because
             * the underlying patch field uses nullable {@link Float} rather than treating zero as "unset".
             */
            public Builder manaCost(float manaCost) {
                this.manaCost = manaCost;
                return this;
            }

            /**
             * Replaces the base ability cast time. Like the other top-level numeric patch values, this is
             * stored as a nullable wrapper so the patch can omit the field entirely when no override is desired.
             */
            public Builder castTime(int castTime) {
                this.castTime = castTime;
                return this;
            }

            public Builder replaceAttribute(String name, JsonElement value) {
                attributeReplacements.put(name, value.deepCopy());
                attributeMerges.remove(name);
                return this;
            }

            public Builder replaceAttribute(String name, Number value) {
                return replaceAttribute(name, new JsonPrimitive(value));
            }

            public Builder replaceAttribute(String name, String value) {
                return replaceAttribute(name, new JsonPrimitive(value));
            }

            public Builder replaceAttribute(String name, boolean value) {
                return replaceAttribute(name, new JsonPrimitive(value));
            }

            public <T> Builder replaceAttribute(String name, Codec<T> codec, T value) {
                return replaceAttribute(name, codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow());
            }

            public Builder replaceAttribute(String name, AbilityFormula formula) {
                return replaceAttribute(name, AbilityFormula.CODEC, formula);
            }

            public Builder replaceAttribute(String name, FormulaParameters parameters) {
                return replaceAttribute(name, FormulaParameters.CODEC, parameters);
            }

            public Builder mergeAttribute(String name, JsonObject value) {
                JsonObject existing = attributeMerges.computeIfAbsent(name, ignored -> new JsonObject());
                mergeJsonObjects(existing, value);
                attributeReplacements.remove(name);
                return this;
            }

            public Builder replaceFormulaParameters(FormulaParameters parameters) {
                return replaceAttribute("formulaParameters", parameters);
            }

            public Builder clearFormulaParameters() {
                return replaceFormulaParameters(FormulaParameters.EMPTY);
            }

            public Builder mergeFormulaParameter(FormulaParameterKey key, float value) {
                JsonObject parameters = new JsonObject();
                parameters.addProperty(key.toString(), value);
                return mergeAttribute("formulaParameters", parameters);
            }

            public AbilityVariantPatch build() {
                return new AbilityVariantPatch(cooldown, manaCost, castTime,
                        Collections.unmodifiableMap(new LinkedHashMap<>(attributeReplacements)),
                        copyMerges(attributeMerges));
            }

            private static Map<String, JsonObject> copyMerges(Map<String, JsonObject> merges) {
                Map<String, JsonObject> output = new LinkedHashMap<>();
                merges.forEach((key, value) -> output.put(key, value.deepCopy()));
                return Collections.unmodifiableMap(output);
            }
        }
    }

    public static class FromMod extends MKAbilityProvider {

        public FromMod(DataGenerator generator, String modId) {
            super(generator, modId);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return CompletableFuture.allOf(
                    MKCoreRegistry.ABILITIES.entrySet().stream()
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

        public Builder ability(DeferredHolder<MKAbility, ? extends MKAbility> abilitySupplier) {
            return new Builder(provider, abilitySupplier);
        }

        public static class Builder {
            private final LanguageProvider provider;
            private final DeferredHolder<MKAbility, ? extends MKAbility> ability;
            private final Map<String, String> customValues = new TreeMap<>();
            private String name;
            private String description;

            public Builder(LanguageProvider provider, DeferredHolder<MKAbility, ? extends MKAbility> supplier) {
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
