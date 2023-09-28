package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkchat.data.DialogueProvider;
import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mknpc.dialogue.effects.OpenLearnAbilitiesEffect;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.HasEntitlementCondition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEntitlements;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class MKUDialogueProvider extends DialogueProvider {


    public MKUDialogueProvider(DataGenerator generator) {
        super(generator, MKUltra.MODID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.allOf(
                writeDialogue(getAlphaMovePrompt(), pOutput),
                writeDialogue(getClericAcolyteDefault(), pOutput),
                writeDialogue(getNetherMageInitiateDefault(), pOutput)
        );
    }

    private DialogueTree getAlphaMovePrompt() {
        var treeBuilder = DialogueTree.builder(new ResourceLocation(MKUltra.MODID, "open_abilities"));

        DialogueNode open = treeBuilder.newNode("open_training")
                .text("Let me see what I can teach you.")
                .effect(new OpenLearnAbilitiesEffect())
                .build();

        DialoguePrompt need = treeBuilder.newPrompt("need_training")
                .trigger("need training")
                .suggest("I need training.")
                .highlight("need training?")
                .respondWith(open)
                .build();

        DialogueNode root = treeBuilder.newNode("root")
                .text("Hello ", PLAYER, ", welcome to the MKU alpha. Do you ").prompt(need)
                .build();

        DialoguePrompt hail = treeBuilder.newPrompt("hail")
                .respondWith(root)
                .build();

        treeBuilder.hail(hail);

        return treeBuilder.build();
    }

    private DialogueTree getNetherMageInitiateDefault() {
        DialogueTree tree = new DialogueTree(new ResourceLocation(MKUltra.MODID, "intro_nether_mage_initiate"));

        DialogueNode open_training = new DialogueNode("open_training", "Let me see what I can teach you.");
        open_training.addEffect(new OpenLearnAbilitiesEffect());
        DialoguePrompt openTraining = new DialoguePrompt("open_training", "teach me", "Will you teach me?", "teach you");
        DialogueResponse resp = new DialogueResponse(open_training);
        resp.addCondition(new HasEntitlementCondition(MKUEntitlements.IntroNetherMageTier1.get()));
        openTraining.addResponse(new DialogueResponse(open_training));


        DialogueNode guildNode = new DialogueNode("guild_desc", "The Nether Mage's Guild studies the " +
                "Fire and Shadow Magics associated with the Nether dimension. We have guild halls all over the place, I'm surprised you haven't heard of us!");
        DialoguePrompt guildPrompt = new DialoguePrompt("nether_mage_guild", "guild", "What guild?", "the Guild");
        guildPrompt.addResponse(new DialogueResponse(guildNode));

        DialogueNode hail_wo_ability = new DialogueNode("hail_wo",
                String.format("Greetings. I am %s, I've been sent here on a mission for %s. ",
                        DialogueContexts.ENTITY_NAME_CONTEXT, guildPrompt.getPromptEmbed()));

        DialogueNode hail_w_ability = new DialogueNode("hail", String.format("Did you want me to %s?.",
                openTraining.getPromptEmbed()));

        DialoguePrompt hailPrompt = new DialoguePrompt("hail", "", "", "");
        DialogueResponse hailWoResp = new DialogueResponse(hail_wo_ability);

        DialogueResponse hailWResp = new DialogueResponse(hail_w_ability);
        hailWResp.addCondition(new HasEntitlementCondition(MKUEntitlements.IntroNetherMageTier1.get()));

        hailPrompt.addResponse(hailWResp);
        hailPrompt.addResponse(hailWoResp);

        tree.addNode(hail_w_ability);
        tree.addNode(hail_wo_ability);
        tree.addNode(open_training);
        tree.addPrompt(hailPrompt);
        tree.addPrompt(openTraining);
        tree.addNode(guildNode);
        tree.addPrompt(guildPrompt);
        tree.setHailPrompt(hailPrompt);
        return tree;
    }

    private DialogueTree getClericAcolyteDefault() {
        var treeBuilder = DialogueTree.builder(new ResourceLocation(MKUltra.MODID, "intro_cleric_acolyte"));

        var openN = treeBuilder.newNode("open_training")
                .text("Let me see what I can teach you.")
                .effect(new OpenLearnAbilitiesEffect())
                .build();

        var openP = treeBuilder.newPrompt("open_training")
                .trigger("magical abilities")
                .suggest("what magical abilities?")
                .highlight("magical abilities")
                .respondWith(openN)
                .build();

        var hailWO = treeBuilder.newNode("hail_wo")
                .text("Greetings. I am ", SPEAKER, ", a humble servant of the Holy See of Solang. ")
                .text("I've been sent here to investigate the undead uprising.")
                .build();

        var hailW = treeBuilder.newNode("hail_w")
                .text("Are you in need of some additional ").prompt(openP).text(" to aid your fight against the undead.")
                .build();

        var hailP = treeBuilder.newPrompt("hail")
                .respondWith(new DialogueResponse(hailW)
                        .addCondition(new HasEntitlementCondition(MKUEntitlements.IntroClericTier1.get())))
                .respondWith(hailWO)
                .build();

        treeBuilder.hail(hailP);
        return treeBuilder.build();
    }
}
