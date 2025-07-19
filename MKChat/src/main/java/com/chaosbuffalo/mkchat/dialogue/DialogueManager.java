package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogueManager {

    @Nullable
    public static DialogueTree getDialogueTree(RegistryAccess registryAccess, ResourceKey<DialogueTree> name) {
        return registryAccess.registryOrThrow(ChatRegistries.DIALOGUE_TREES).get(name);
    }

    // Matches {namespace:target}, allowed chars [a-zA-Z0-9_-.] allowed chars for target also include : so that we can
    // handle resource locations for instance the item name provider
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\{(?<namespace>[\\w-.]+):(?<target>[\\w-.:]+)}");

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
