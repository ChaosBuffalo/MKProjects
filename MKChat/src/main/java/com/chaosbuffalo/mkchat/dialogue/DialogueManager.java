package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasBoolFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogueManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "dialogues";

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, DialogueTree> trees = new HashMap<>();
    private static final Map<String, BiFunction<String, DialogueTree, Component>> textComponentProviders = new HashMap<>();
    private static final Map<String, Function<DialogueContext, Component>> contextProviders = new HashMap<>();

    private static final Map<ResourceLocation, Supplier<DialogueEffect>> effectDeserializers = new HashMap<>();
    private static final Map<ResourceLocation, Supplier<DialogueCondition>> conditionDeserializers = new HashMap<>();

    private static Component playerNameProvider(DialogueContext context) {
        return context.player().getName();
    }

    private static Component entityNameProvider(DialogueContext context) {
        return context.speaker().getName();
    }

    private static Component contextProvider(String name, DialogueTree tree) {
        var supplier = contextProviders.get(name);
        if (supplier != null) {
            return DialogueComponentContents.create(supplier);
        } else {
            return null;
        }
    }

    private static Component promptProvider(String name, DialogueTree tree) {
        DialoguePrompt prompt = tree.getPrompt(name);
        if (prompt != null) {
            return prompt.getPromptLink();
        } else {
            return null;
        }
    }

    private static Component itemProvider(String name, DialogueTree tree) {
        ResourceLocation itemId = new ResourceLocation(name);
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item != null) {
            return Component.translatable(item.getDescriptionId());
        } else {
            return null;
        }
    }

    public static void dialogueSetup() {
        putEffectDeserializer(AddLevelEffect.effectTypeName, AddLevelEffect::new);
        putEffectDeserializer(AddFlag.effectTypeName, AddFlag::new);
        putConditionDeserializer(HasBoolFlagCondition.conditionTypeName, HasBoolFlagCondition::new);
        putTextComponentProvider("context", DialogueManager::contextProvider);
        putTextComponentProvider("prompt", DialogueManager::promptProvider);
        putTextComponentProvider("item", DialogueManager::itemProvider);
        putContextArgProvider("player_name", DialogueManager::playerNameProvider);
        putContextArgProvider("entity_name", DialogueManager::entityNameProvider);
    }

    public static void putContextArgProvider(String typeName, Function<DialogueContext, Component> func) {
        contextProviders.put(typeName, func);
    }

    public static void putEffectDeserializer(ResourceLocation typeName, Supplier<DialogueEffect> func) {
        effectDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueEffect getDialogueEffect(ResourceLocation effectType) {

        if (!effectDeserializers.containsKey(effectType)) {
            MKChat.LOGGER.error("Failed to deserialize dialogue effect {}", effectType);
            return null;
        }
        return effectDeserializers.get(effectType).get();
    }

    public static void putConditionDeserializer(ResourceLocation typeName, Supplier<DialogueCondition> func) {
        conditionDeserializers.put(typeName, func);
    }

    @Nullable
    public static DialogueCondition getDialogueCondition(ResourceLocation conditionType) {
        if (!conditionDeserializers.containsKey(conditionType)) {
            MKChat.LOGGER.error("Failed to deserialize dialogue condition {}", conditionType);
            return null;
        }
        return conditionDeserializers.get(conditionType).get();
    }

    public static void putTextComponentProvider(String typeName, BiFunction<String, DialogueTree, Component> func) {
        textComponentProviders.put(typeName, func);
    }

    public static Map<ResourceLocation, DialogueTree> getTrees() {
        return trees;
    }

    public DialogueManager() {
        super(GSON, DEFINITION_FOLDER);
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn,
                         @Nullable ResourceManager resourceManagerIn,
                         @Nullable ProfilerFiller profilerIn) {
        trees.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKChat.LOGGER.info("Found dialogue tree file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            DialogueTree tree = DialogueTree.deserializeTreeFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            trees.put(tree.getDialogueName(), tree);
        }
    }

    // Matches {namespace:target}, allowed chars [a-zA-Z0-9_-]
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\{(?<namespace>[\\w-]+):(?<target>[\\w-]+)}");

    private static void decomposeString(String rawString,
                                        BiFunction<String, String, Component> valueProvider,
                                        Consumer<Component> outputConsumer) {
        Matcher matcher = FORMAT_PATTERN.matcher(rawString);

        int nextStart;
        int mEnd;
        for(nextStart = 0; matcher.find(nextStart); nextStart = mEnd) {
            int mStart = matcher.start();
            mEnd = matcher.end();
            if (mStart > nextStart) {
                String head = rawString.substring(nextStart, mStart);
                outputConsumer.accept(Component.literal(head));
            }

            String namespace = matcher.group("namespace");
            String target = matcher.group("target");

            Component newValue = valueProvider.apply(namespace, target);
            if (newValue != null) {
                outputConsumer.accept(newValue);
            } else {
                String rawOrigValue = rawString.substring(mStart, mEnd);

                Component replacedValue = Component.literal(rawOrigValue);
                outputConsumer.accept(replacedValue);
            }
        }

        if (nextStart < rawString.length()) {
            String tail = rawString.substring(nextStart);
            outputConsumer.accept(Component.literal(tail));
        }
    }

    public static Component parseDialogueMessage(String rawMessage, DialogueTree tree) {
        MutableComponent parsed = Component.empty();

        decomposeString(rawMessage, (namespace, target) -> {
            var provider = textComponentProviders.get(namespace);
            if (provider != null) {
                return provider.apply(target, tree);
            } else {
                return null;
            }
        }, parsed::append);

        return parsed;
    }

    @Nullable
    public static DialogueTree getDialogueTree(ResourceLocation name) {
        return trees.get(name);
    }
}
