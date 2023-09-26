package com.chaosbuffalo.mkchat;

import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasBoolFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChatRegistries {

    private static final Map<String, BiFunction<String, DialogueTree, Component>> textComponentProviders = new HashMap<>();
    private static final Map<String, Function<DialogueContext, Component>> contextProviders = new HashMap<>();

    private static final Map<ResourceLocation, Supplier<DialogueEffect>> effectDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, Supplier<DialogueCondition>> conditionDeserializers = new HashMap<>();

    public static void putEffectDeserializer(ResourceLocation typeName, Supplier<DialogueEffect> func) {
        effectDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueEffect createDialogueEffect(ResourceLocation effectType) {
        var factory = effectDeserializers.get(effectType);
        if (factory == null) {
            MKChat.LOGGER.error("Failed to deserialize dialogue effect {}", effectType);
            return null;
        }
        return factory.get();
    }

    public static void putConditionDeserializer(ResourceLocation typeName, Supplier<DialogueCondition> func) {
        conditionDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueCondition createDialogueCondition(ResourceLocation conditionType) {
        var factory = conditionDeserializers.get(conditionType);
        if (factory == null) {
            MKChat.LOGGER.error("Failed to deserialize dialogue condition {}", conditionType);
            return null;
        }
        return factory.get();
    }

    public static void putTextComponentProvider(String typeName, BiFunction<String, DialogueTree, Component> func) {
        textComponentProviders.put(typeName, func);
    }

    @Nullable
    public static BiFunction<String, DialogueTree, Component> getTextProvider(String name) {
        return textComponentProviders.get(name);
    }

    public static void putContextArgProvider(String typeName, Function<DialogueContext, Component> func) {
        contextProviders.put(typeName, func);
    }

    @Nullable
    public static Function<DialogueContext, Component> getDialogueContextHandler(String name) {
        return contextProviders.get(name);
    }

    public static void setup() {
        putEffectDeserializer(AddLevelEffect.effectTypeName, AddLevelEffect::new);
        putEffectDeserializer(AddFlag.effectTypeName, AddFlag::new);
        putConditionDeserializer(HasBoolFlagCondition.conditionTypeName, HasBoolFlagCondition::new);
        putTextComponentProvider("context", DialogueProviders::contextProvider);
        putTextComponentProvider("prompt", DialogueProviders::promptProvider);
        putTextComponentProvider("item", DialogueProviders::itemProvider);
        putContextArgProvider("player_name", DialogueProviders::playerNameProvider);
        putContextArgProvider("entity_name", DialogueProviders::entityNameProvider);
    }
}
