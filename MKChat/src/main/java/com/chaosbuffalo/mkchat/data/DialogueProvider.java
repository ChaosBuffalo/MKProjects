package com.chaosbuffalo.mkchat.data;

import com.chaosbuffalo.mkchat.dialogue.DialogueContexts;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public abstract class DialogueProvider extends MKDataProvider {

    protected static final String PLAYER = DialogueContexts.PLAYER_NAME_CONTEXT;
    protected static final String SPEAKER = DialogueContexts.ENTITY_NAME_CONTEXT;

    public DialogueProvider(DataGenerator generator, String modId) {
        super(generator, modId, "Dialogue");
    }


    public CompletableFuture<?> writeDialogue(DialogueTree dialogue, CachedOutput cachedOutput) {
        Path outputFolder = generator.getPackOutput().getOutputFolder();
        ResourceLocation key = dialogue.getDialogueName();
        Path local = Paths.get("data", key.getNamespace(), DialogueManager.DEFINITION_FOLDER, key.getPath() + ".json");
        Path path = outputFolder.resolve(local);
        JsonElement element = dialogue.serialize(JsonOps.INSTANCE);
        return DataProvider.saveStable(cachedOutput, element, path);
    }
}
