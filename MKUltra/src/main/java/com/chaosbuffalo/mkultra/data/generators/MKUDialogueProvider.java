package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mknpc.dialogue.effects.OpenLearnAbilitiesEffect;
import com.chaosbuffalo.mknpc.quest.DialogueBuilder;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.HasEntitlementCondition;
import com.chaosbuffalo.mkultra.init.MKUDialogues;
import com.chaosbuffalo.mkultra.init.MKUEntitlements;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class MKUDialogueProvider {


    public static void bootstrap(BootstrapContext<DialogueTree> context) {
        context.register(MKUDialogues.OPEN_ABILITIES, getAlphaMovePrompt(MKUDialogues.OPEN_ABILITIES));
        context.register(MKUDialogues.intro_nether_mage_initiate, getNetherMageInitiateDefault(MKUDialogues.intro_nether_mage_initiate, context));
        context.register(MKUDialogues.intro_cleric_acolyte, getClericAcolyteDefault(MKUDialogues.intro_cleric_acolyte, context));
        context.register(MKUDialogues.cleric_default, clericDefault(MKUDialogues.cleric_default, context));
        context.register(MKUDialogues.necro_default, necroDefault(MKUDialogues.necro_default, context));
    }

    private static DialogueTree getAlphaMovePrompt(ResourceKey<DialogueTree> key) {
        var treeBuilder = DialogueTree.builder(key);

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
                .text("Hello ", DialogueContexts.PLAYER_NAME_CONTEXT, ", welcome to the MKU beta. Do you ").prompt(need)
                .build();

        DialoguePrompt hail = treeBuilder.newPrompt("hail")
                .respondWith(root)
                .build();

        treeBuilder.hail(hail);

        return treeBuilder.build();
    }

    private static DialogueTree getNetherMageInitiateDefault(ResourceKey<DialogueTree> key, BootstrapContext<DialogueTree> context) {
        var entitlements = context.lookup(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);
        DialogueTree tree = new DialogueTree(key);

        DialogueNode open_training = new DialogueNode("open_training", "Let me see what I can teach you.");
        open_training.addEffect(new OpenLearnAbilitiesEffect());
        DialoguePrompt openTraining = new DialoguePrompt("open_training", "teach me", "Will you teach me?", "teach you");
        DialogueResponse resp = new DialogueResponse(open_training);
        resp.addCondition(new HasEntitlementCondition(entitlements.getOrThrow(MKUEntitlements.IntroNetherMageTier1)));
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
        hailWResp.addCondition(new HasEntitlementCondition(entitlements.getOrThrow(MKUEntitlements.IntroNetherMageTier1)));

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

    private static DialogueTree getClericAcolyteDefault(ResourceKey<DialogueTree> key, BootstrapContext<DialogueTree> context) {
        var entitlements = context.lookup(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);

        var treeBuilder = DialogueTree.builder(key);

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
                .text("Greetings. I am ", DialogueContexts.ENTITY_NAME_CONTEXT, ", a humble servant of the Holy See of Solang. ")
                .text("I've been sent here to investigate the undead uprising.")
                .build();

        var hailW = treeBuilder.newNode("hail_w")
                .text("Are you in need of some additional ").prompt(openP).text(" to aid your fight against the undead.")
                .build();

        var hailP = treeBuilder.newPrompt("hail")
                .respondWith(new DialogueResponse(hailW)
                        .addCondition(new HasEntitlementCondition(entitlements.getOrThrow(MKUEntitlements.IntroClericTier1))))
                .respondWith(hailWO)
                .build();

        treeBuilder.hail(hailP);
        return treeBuilder.build();
    }

    private static DialogueTree clericDefault(ResourceKey<DialogueTree> key, BootstrapContext<DialogueTree> context) {
        var entitlements = context.lookup(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);

        var treeBuilder = DialogueBuilder.hailWithCondition("Hail and well met {player}, are you in need of [training|I need training]?",
                        "I am {name}, Solang's Servant for this temple. May His Light guide you.",
                new HasEntitlementCondition(entitlements.getOrThrow(MKUEntitlements.ClericTier1)))
                .effectNode("training", "Let's see what I can teach you", new OpenLearnAbilitiesEffect())
                .context("name", DialogueContexts.ENTITY_NAME_CONTEXT)
                .context("player", DialogueContexts.PLAYER_NAME_CONTEXT);

        return treeBuilder.build().buildStandalone(key);
    }

    private static DialogueTree necroDefault(ResourceKey<DialogueTree> key, BootstrapContext<DialogueTree> context) {
        var entitlements = context.lookup(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);

        var treeBuilder = DialogueBuilder.hailWithCondition("Darkness has brought you here {player}, what do you hear in the [whispers|whisper|They whisper to me of strength and decay.]?",
                        "The necromantic arts can sap strength from sinew and carve flesh with ease.",
                        new HasEntitlementCondition(entitlements.getOrThrow(MKUEntitlements.ThemcromancerTier1)))
                .effectNode("whisper", "The power we receive from darkness is beyond the ken of mortals.", new OpenLearnAbilitiesEffect())
                .context("name", DialogueContexts.ENTITY_NAME_CONTEXT)
                .context("player", DialogueContexts.PLAYER_NAME_CONTEXT);

        return treeBuilder.build().buildStandalone(key);
    }
}
