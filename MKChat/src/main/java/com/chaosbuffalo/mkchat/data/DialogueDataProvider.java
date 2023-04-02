package com.chaosbuffalo.mkchat.data;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasBoolFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlag;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DialogueDataProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public DialogueDataProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(@Nonnull HashCache cache) {
        writeDialogue(getTestTree(), cache);
    }

    private DialogueTree getTestTree() {
        DialogueTree tree = new DialogueTree(new ResourceLocation(MKChat.MODID, "test"));
        DialogueNode grantLevel = new DialogueNode("grant_level", "Here is 1 level.");
        grantLevel.addEffect(new AddLevelEffect(1));
        ResourceLocation levelFlag = new ResourceLocation(MKChat.MODID, "grant_level");
        grantLevel.addEffect(new AddFlag(levelFlag));

        tree.addNode(grantLevel);

        DialogueNode alreadyGranted = new DialogueNode("already_granted",
                "You already got a level, don't be greedy.");

        DialogueNode cantHelp = new DialogueNode("cant_help",
                "I have already helped you as much as I can.");

        tree.addNode(alreadyGranted);
        tree.addNode(cantHelp);

        DialoguePrompt needXp = new DialoguePrompt("need_xp", "need xp",
                "I need xp.", "need some xp");
        needXp.addResponse(new DialogueResponse("grant_level")
                .addCondition(new HasBoolFlagCondition(levelFlag).setInvert(true)));
        needXp.addResponse(new DialogueResponse("already_granted")
                .addCondition(new HasBoolFlagCondition(levelFlag)));

        tree.addPrompt(needXp);

        DialoguePrompt hail = new DialoguePrompt("hail", "", "", "")
                .addResponse(new DialogueResponse("root")
                        .addCondition(new HasBoolFlagCondition(levelFlag).setInvert(true)))
                .addResponse(new DialogueResponse("cant_help")
                        .addCondition(new HasBoolFlagCondition(levelFlag))
                );

        tree.addPrompt(hail);

        DialogueNode root = new DialogueNode("root", String.format("Hello %s, I am %s. Do you %s",
                DialogueContexts.PLAYER_NAME_CONTEXT, DialogueContexts.ENTITY_NAME_CONTEXT, needXp.getPromptEmbed()));

        tree.addNode(root);

        tree.setHailPrompt(hail);

        return tree;
    }


    public void writeDialogue(DialogueTree dialogue, @Nonnull HashCache cache) {
        Path outputFolder = this.generator.getOutputFolder();
        ResourceLocation key = dialogue.getDialogueName();
        Path local = Paths.get("data", key.getNamespace(), DialogueManager.DEFINITION_FOLDER, key.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        try {
            JsonElement element = dialogue.serialize(JsonOps.INSTANCE);
            DataProvider.save(GSON, cache, element, path);
        } catch (IOException e) {
            MKChat.LOGGER.error("Couldn't write dialogue to file {}", path, e);
        }
    }

    @Override
    public String getName() {
        return "MKChat Dialogues";
    }
}
