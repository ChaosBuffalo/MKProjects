package com.chaosbuffalo.mkchat;

import com.chaosbuffalo.mkchat.dialogue.DialogueContext;
import com.chaosbuffalo.mkchat.dialogue.DialogueProviders;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasBoolFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChatRegistries {
    public static final ResourceLocation EFFECT_TYPES_REGISTRY_NAME = new ResourceLocation(MKChat.MODID, "dialogue_effect_types");
    public static IForgeRegistry<DialogueEffectType<?>> DIALOGUE_EFFECTS = null;


    private static final Map<String, BiFunction<String, DialogueTree, Component>> textComponentProviders = new HashMap<>();
    private static final Map<String, Function<DialogueContext, Component>> contextProviders = new HashMap<>();

    private static final Map<ResourceLocation, Supplier<DialogueCondition>> conditionDeserializers = new HashMap<>();

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
        putConditionDeserializer(HasBoolFlagCondition.conditionTypeName, HasBoolFlagCondition::new);
        putTextComponentProvider("context", DialogueProviders::contextProvider);
        putTextComponentProvider("prompt", DialogueProviders::promptProvider);
        putTextComponentProvider("item", DialogueProviders::itemProvider);
        putContextArgProvider("player_name", DialogueProviders::playerNameProvider);
        putContextArgProvider("entity_name", DialogueProviders::entityNameProvider);
    }

    public static void createRegistries(NewRegistryEvent event) {
        event.create(new RegistryBuilder<DialogueEffectType<?>>()
                .setName(EFFECT_TYPES_REGISTRY_NAME), r -> DIALOGUE_EFFECTS = r);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ChatRegistries::createRegistries);
        DialogueEffectTypes.REGISTRY.register(modBus);
    }
}
