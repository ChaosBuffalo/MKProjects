package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.MKChat;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogueManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "dialogues";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, DialogueTree> trees = new HashMap<>();

    public DialogueManager() {
        super(GSON, DEFINITION_FOLDER);
    }

    public static Map<ResourceLocation, DialogueTree> getTrees() {
        return trees;
    }

    @Nullable
    public static DialogueTree getDialogueTree(ResourceLocation name) {
        return trees.get(name);
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
        for (nextStart = 0; matcher.find(nextStart); nextStart = mEnd) {
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
            var provider = ChatRegistries.getTextProvider(namespace);
            if (provider != null) {
                return provider.apply(target, tree);
            } else {
                return null;
            }
        }, parsed::append);

        return parsed;
    }
}
