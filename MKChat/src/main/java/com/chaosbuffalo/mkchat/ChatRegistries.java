package com.chaosbuffalo.mkchat;

import com.chaosbuffalo.mkchat.dialogue.DialogueContext;
import com.chaosbuffalo.mkchat.dialogue.DialogueProviders;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionTypes;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectTypes;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChatRegistries {
    public static final ResourceKey<Registry<DialogueEffectType<?>>> EFFECT_TYPES_REGISTRY_NAME = ResourceKey.createRegistryKey(MKChat.id("dialogue_effect_types"));
    public static final ResourceKey<Registry<DialogueConditionType<?>>> CONDITION_TYPES_REGISTRY_NAME = ResourceKey.createRegistryKey(MKChat.id("dialogue_condition_types"));
    public static final Registry<DialogueEffectType<?>> DIALOGUE_EFFECTS = new RegistryBuilder<>(EFFECT_TYPES_REGISTRY_NAME).create();
    public static final Registry<DialogueConditionType<?>> DIALOGUE_CONDITIONS = new RegistryBuilder<>(CONDITION_TYPES_REGISTRY_NAME).create();


    private static final Map<String, BiFunction<String, DialogueTree, Component>> textComponentProviders = new HashMap<>();
    private static final Map<String, Function<DialogueContext, Component>> contextProviders = new HashMap<>();

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
        putTextComponentProvider("context", DialogueProviders::contextProvider);
        putTextComponentProvider("prompt", DialogueProviders::promptProvider);
        putTextComponentProvider("item", DialogueProviders::itemProvider);
        putContextArgProvider("player_name", DialogueProviders::playerNameProvider);
        putContextArgProvider("entity_name", DialogueProviders::entityNameProvider);
    }

    public static void createRegistries(NewRegistryEvent event) {
        event.register(DIALOGUE_EFFECTS);
        event.register(DIALOGUE_CONDITIONS);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ChatRegistries::createRegistries);
        DialogueEffectTypes.REGISTRY.register(modBus);
        DialogueConditionTypes.REGISTRY.register(modBus);
    }
}
