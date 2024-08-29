package com.chaosbuffalo.mkchat.dialogue;

import com.chaosbuffalo.mkchat.ChatRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class DialogueProviders {
    public static Component playerNameProvider(DialogueContext context) {
        return context.player().getName();
    }

    public static Component entityNameProvider(DialogueContext context) {
        return context.speaker().getName();
    }

    public static Component promptProvider(String name, DialogueTree tree) {
        DialoguePrompt prompt = tree.getPrompt(name);
        if (prompt != null) {
            return prompt.getPromptLink();
        } else {
            return null;
        }
    }

    public static Component itemProvider(String name, DialogueTree tree) {
        ResourceLocation itemId = ResourceLocation.parse(name);
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item != null) {
            return Component.translatable(item.getDescriptionId());
        } else {
            return null;
        }
    }

    public static Component contextProvider(String name, DialogueTree tree) {
        var supplier = ChatRegistries.getDialogueContextHandler(name);
        if (supplier != null) {
            return DialogueComponentContents.create(supplier);
        } else {
            return null;
        }
    }
}
