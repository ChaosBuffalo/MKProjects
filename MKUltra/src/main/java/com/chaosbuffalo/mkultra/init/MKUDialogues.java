package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.generators.MKUDialogueProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class MKUDialogues {

    static ResourceKey<DialogueTree> key(String path) {
        return ResourceKey.create(ChatRegistries.DIALOGUE_TREES, MKUltra.id(path));
    }

    public static final ResourceKey<DialogueTree> OPEN_ABILITIES = key("open_abilities");
    public static final ResourceKey<DialogueTree> cleric_default = key("cleric_default");
    public static final ResourceKey<DialogueTree> intro_nether_mage_initiate = key("intro_nether_mage_initiate");
    public static final ResourceKey<DialogueTree> intro_cleric_acolyte = key("intro_cleric_acolyte");
    public static final ResourceKey<DialogueTree> necro_default = key("necro_default");


    public static void bootstrap(BootstrapContext<DialogueTree> context) {
        MKUDialogueProvider.bootstrap(context);
    }
}
