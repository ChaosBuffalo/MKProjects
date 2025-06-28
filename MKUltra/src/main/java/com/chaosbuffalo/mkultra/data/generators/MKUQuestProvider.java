package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkfaction.faction.MKFactionRegistry;
import com.chaosbuffalo.mknpc.data.providers.QuestDefinitionProvider;
import com.chaosbuffalo.mknpc.dialogue.effects.OpenLearnAbilitiesEffect;
import com.chaosbuffalo.mknpc.quest.*;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.HasSpentTalentPointsCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.HasTrainedAbilitiesCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.conditions.HasWeaponInHandCondition;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.ObjectiveCompleteEffect;
import com.chaosbuffalo.mknpc.quest.objectives.QuestLootTypeTagObjective;
import com.chaosbuffalo.mknpc.quest.objectives.TradeItemsObjective;
import com.chaosbuffalo.mknpc.quest.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mknpc.quest.rewards.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.data.registries.UltraStructures;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEntitlements;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MKUQuestProvider extends QuestDefinitionProvider {

    public MKUQuestProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> provider) {
        super(generator, provider, MKUltra.MODID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                writeDefinition(generateIntroQuest(), cache),
                writeDefinition(generateTrooperArmorQuest(), cache),
                writeDefinition(generateIntroClericQuest(), cache),
                writeDefinition(generateIntroMageQuest(), cache),
                writeDefinition(this::generateClericQuestChain, cache),
                writeDefinition(this::generateJoinThemcromancers, cache),
                writeDefinition(this::generateThemcromancerChain, cache)
        );
    }

    private QuestDefinition generateThemcromancerChain(HolderLookup.Provider provider) {
        var factionReg = provider.lookupOrThrow(MKFactionRegistry.FACTION_REGISTRY_KEY);

        QuestStructureLocation temple = new QuestStructureLocation(UltraStructures.THEMCROMANCERS_LAIR.location(), "0");
        QuestStructureLocation obelisk = new QuestStructureLocation(UltraStructures.DEEPSLATE_OBELISK.location(), "0");
        QuestStructureLocation solangTemple = new QuestStructureLocation(UltraStructures.DESERT_TEMPLE_VILLAGE.location(), "0");
        QuestBuilder.QuestNpc archon = new QuestBuilder.QuestNpc(temple, MKUltra.id("themcromancer_archon"));
        QuestBuilder.QuestNpc solangTempleGuard = new QuestBuilder.QuestNpc(solangTemple, MKUltra.id("solangian_temple_guard_2"));


        QuestDefinition def = new QuestDefinition(MKUltra.id("necromancer_unlock_chain"));
        def.setRepeatable(false);
        def.setQuestName(Component.literal("Path to Them"));


        DialogueBuilder start = DialogueBuilder.hail(
                        "What has brought you to this temple child? I, {name}, humble Servant of Them have many mysteries to contemplate. " +
                                "Begone unless you can be of [service|How can I be of service?].")
                .node("service", "There is always work to be done to keep the constructs in working order. Perhaps you could" +
                        "[collect|I will collect the parts.] some necessary parts.")
                .node("collect", "Go out in the dead of night and bring back everything on this list. Complete this task and " +
                        "I will set you upon the path.")
                .context("name", DialogueContexts.ENTITY_NAME_CONTEXT);

        start.build().populateStart(def, "collect");

        Quest parts = new QuestBuilder("parts",
                Component.literal("The Archon has provided you with a shopping list. Destroy undead in the wilderness and collect their parts."))
                .questLootFromTypeTag("badly_damaged_skulls", EntityTypeTags.UNDEAD, "the Undead",
                        10, 0.50, Component.literal("Badly Damaged Skull"))
                .questLootFromTypeTag("femurs", EntityTypeTags.UNDEAD, "the Undead",
                        4, 0.10, Component.literal("Cracked Femur"))
                .questLootFromTypeTag("finger_bones", EntityTypeTags.UNDEAD, "the Undead",
                        6, 0.25, Component.literal("Decaying Fingerbones"))
                .autoComplete(true)
                .reward(new GrantEntitlementReward(MKUEntitlements.ThemcromancerTier1))
                .xp(250)
                .quest();
        parts.setAutoComplete(true);
        def.addQuest(parts);

        DialogueBuilder postParts = DialogueBuilder.hail(
                "You have performed adequately. I will [teach|Will you teach me?] you some of the basics of necromancy. There is" +
                        "another [task|What task?] for you to complete.")
                .effectNode("task", "Go and find an obelisk surrounded by seafury skeletons. Collect the remnants of the sea from their bones.",
                        new ObjectiveCompleteEffect("return_to_archon", "return_to_archon"))
                .effectNode("teach", "The necromantic arts can sap strength from sinew and carve flesh with ease.",
                        new OpenLearnAbilitiesEffect());

        Quest return1 = new QuestBuilder("return_to_archon",
                Component.literal("Return to the Archon"))
                .autoComplete(true)
                .builderHail("return_to_archon", Component.literal("Talk to the Archon again."),
                        archon,
                        postParts,
                        null
                )
                .reward(new XpReward(50))
                .reward(new FactionReward(100, factionReg.getOrThrow(MKUFactions.THEMCROMANCERS_NAME)))
                .quest();
        def.addQuest(return1);

        Quest remnants = new QuestBuilder("collect_remnants",
                Component.literal("Collect the remnants of the sea."))
                .autoComplete(true)
                .questLootFromDef("whispers", obelisk, MKUltra.id("seawoven_wretch"),
                        0.25, 5, Component.literal("Whispers of Sea Foam"))
                .questLootFromDef("echoes", obelisk, MKUltra.id("seawoven_skeleton"),
                        0.2, 8, Component.literal("Echoes of Dead Waves"))
                .reward(new XpReward(250))
                .quest();
        def.addQuest(remnants);

        DialogueBuilder postRemnants = DialogueBuilder.hail(
                        "Ahhhh... Even now, I can still smell the breath of those ancient seamen on you. " +
                                "Did you feel the spray of waves from centuries long past as you sifted through their remains?" +
                                "Now I will teach you even more secrets of necromancy. However, to become a full-fledged Necromancer, you must [prove|How can I prove myself?] yourself.")
                .node("prove", "Go to the temple of the God Solang and kill {temple_guard}. Beware, the Clerics will know of your intent should you accept. Are you [ready|I am ready.]?")
                .effectNode("ready", "Go swiftly, kill the Temple Cleric, and return here.",
                        new ObjectiveCompleteEffect("return_after_remnants", "return_after_remnants"))
                .context("temple_guard", solangTempleGuard.getDialogueLink());


        Quest returnAfterRemnants = new QuestBuilder("return_after_remnants",
                Component.literal("Return to the Archon"))
                .autoComplete(true)
                .builderHail("return_after_remnants", Component.literal("Talk to the Archon again."),
                        archon,
                        postRemnants,
                        null
                )
                .reward(new FactionReward(100, factionReg.getOrThrow(MKUFactions.THEMCROMANCERS_NAME)))
                .reward(new FactionReward(-5000, factionReg.getOrThrow(MKUFactions.SEE_OF_SOLANG_NAME)))
                .reward(new GrantEntitlementReward(MKUEntitlements.ThemcromancerTier2))
                .reward(new XpReward(250))
                .quest();
        def.addQuest(returnAfterRemnants);

        Quest killTempleGuard = new QuestBuilder("kill_temple_guard", Component.literal("Eliminate the Temple Guard"))
                .autoComplete(true)
                .killNotable("kill_temple_guard", solangTempleGuard)
                .reward(new XpReward(500))
                .reward(new FactionReward(500, factionReg.getOrThrow(MKUFactions.THEMCROMANCERS_NAME)))
                .quest();

        def.addQuest(killTempleGuard);


        DialogueBuilder postKill2 = DialogueBuilder
                .hail("You have proven yourself to me, and to the King of Darkness. I will now teach you our most secret technique. " +
                        "Go forth and continue to spread the Will.", true);

        Quest afterKill2 = new QuestBuilder("after_kill_2",
                Component.literal("Return to the Archon"))
                .autoComplete(true)
                .builderHail("return_after_kill_2", Component.literal("Talk to the Archon again."),
                        archon, postKill2, null)
                .reward(new FactionReward(200, factionReg.getOrThrow(MKUFactions.SEE_OF_SOLANG_NAME)))
                .reward(new GrantEntitlementReward(MKUEntitlements.ThemcromancerTier3))
                .reward(new XpReward(400))
                .quest();

        def.addQuest(afterKill2);

        return def;
    }

    private QuestDefinition generateJoinThemcromancers(HolderLookup.Provider provider) {
        var factionReg = provider.lookupOrThrow(MKFactionRegistry.FACTION_REGISTRY_KEY);
        QuestStructureLocation lair = new QuestStructureLocation(UltraStructures.THEMCROMANCERS_LAIR.location(), "0");
        QuestBuilder.QuestNpc gatekeeper = new QuestBuilder.QuestNpc(lair, MKUltra.id("a_skeletal_gatekeeper"));
        QuestBuilder.QuestNpc archon = new QuestBuilder.QuestNpc(lair, MKUltra.id("themcromancer_archon"));

        QuestDefinition def = new QuestDefinition(MKUltra.id("unlock_themcromancers"));
        def.setRepeatable(false);
        def.setQuestName(Component.literal("Supplying Materials"));

        DialogueBuilder start = DialogueBuilder.hail(
                "You look upon a great temple to Them, Mortal. You will not be able to walk with the chosen without " +
                        "[proving|prove|How can I prove my worth?] your worth.")
                .node("prove", "The acolytes are always in need of more... [materials|What materials?] for their experiments.")
                .node("materials", "Our work makes use of {bones} and {flesh}, bring some to me and you will be granted entry.")
                .context("bones", DialogueUtils.getStackCountItemProvider(new ItemStack(Items.BONE, 64)))
                .context("flesh", DialogueUtils.getStackCountItemProvider(new ItemStack(Items.ROTTEN_FLESH, 64)));

        start.build().populateStart(def, "materials");

        Quest bonesAndFlesh = new Quest("bones_flesh", text("You must deliver a grisly harvest to the skeletal gatekeeper."));
        bonesAndFlesh.setAutoComplete(true);
        TradeItemsObjective tradeGold = new TradeItemsObjective(
                "trade_bones_flesh",
                lair,
                gatekeeper.npcDef,
                List.of(
                        new ItemStack(Items.BONE, 64),
                        new ItemStack(Items.ROTTEN_FLESH, 64)
                ));
        bonesAndFlesh.addObjective(tradeGold);
        bonesAndFlesh.addReward(new XpReward(100));
        bonesAndFlesh.addReward(new FactionReward(1000, factionReg.getOrThrow(MKUFactions.THEMCROMANCERS_NAME)));
        def.addQuest(bonesAndFlesh);

        DialogueBuilder visitArchon = DialogueBuilder.hail("You will be allowed onto the grounds now. You should seek out the [archon|Who is the archon?] to learn more of our art.")
                .effectNode("archon", "{archon_name} is in charge of this temple. There are others like it all over the world, you will find you have access to them all. " +
                        "The Archon will be in the inner sanctum of the temple.",
                        new ObjectiveCompleteEffect("return_to_gatekeeper", "return_to_gatekeeper"))
                .context("archon_name", archon.getDialogueLink());


        Quest return1 = new QuestBuilder("return_to_gatekeeper",
                Component.literal("Return to the gatekeeper"))
                .autoComplete(true)
                .builderHail("return_to_gatekeeper", Component.literal("Talk to the skeletal gatekeeper again."),
                        gatekeeper,
                        visitArchon,
                        null
                )
                .reward(new XpReward(50))
                .reward(new FactionReward(1000, factionReg.getOrThrow(MKUFactions.THEMCROMANCERS_NAME)))
                .quest();
        def.addQuest(return1);
        //this quest does not reference this character in any particular objective but we need it to generate the dialogue
        def.addAdditionalNotable(lair, archon.npcDef);
        return def;
    }

    private QuestDefinition generateClericQuestChain(HolderLookup.Provider provider) {
        var factionReg = provider.lookupOrThrow(MKFactionRegistry.FACTION_REGISTRY_KEY);

        QuestStructureLocation temple = new QuestStructureLocation(UltraStructures.DESERT_TEMPLE_VILLAGE.location(), "0");
        QuestStructureLocation tomb = new QuestStructureLocation(UltraStructures.HYBOREAN_CRYPT.location(), "0");
        QuestBuilder.QuestNpc cleric = new QuestBuilder.QuestNpc(temple, MKUltra.id("solangian_cleric"));
        QuestBuilder.QuestNpc sorcerer_queen = new QuestBuilder.QuestNpc(tomb, MKUltra.id("hyborean_sorcerer_queen"));
        QuestBuilder.QuestNpc ancient_king = new QuestBuilder.QuestNpc(tomb, MKUltra.id("an_ancient_king"));

        QuestDefinition def = new QuestDefinition(MKUltra.id("cleric_unlock_chain"));
        def.setRepeatable(false);
        def.setQuestName(Component.literal("Seeking the Light"));


        DialogueBuilder start = DialogueBuilder.hail(
                "Hail and well met traveler. I'm {name} and I welcome you to our humble [temple|Tell me about this temple.].")
                .node("temple", "This temple is dedicated to the worship of His Holy Radiance: Solang, " +
                        "God of the Sun, Bringer of the Morning Light, Banisher of the Dead. According to our records it doesn't look" +
                        " like you've ever [tithed|What do you mean by tithed?].")
                .node("tithed", "There are many expenses in the pursuit of our mission to rid this world of the restless dead. Perhaps you would like to [contribute|I can contribute]?")
                .node("contribute", "A donation of {10 gold bars} would allow us to continue arming the templars and supporting the community here.")
                .context("name", DialogueContexts.ENTITY_NAME_CONTEXT)
                .context("10 gold bars", DialogueUtils.getStackCountItemProvider(new ItemStack(Items.GOLD_INGOT, 10)));

        start.build().populateStart(def, "contribute");

        Quest goldBars = new Quest("gold_bars", text("The Temple of Solang desires a donation of gold."));
        goldBars.setAutoComplete(true);
        TradeItemsObjective tradeGold = new TradeItemsObjective(
                "trade_gold_bars",
                temple,
                cleric.npcDef,
                List.of(
                        new ItemStack(Items.GOLD_INGOT, 10)
                ));
        goldBars.addObjective(tradeGold);
        goldBars.addReward(new XpReward(100));
        goldBars.addReward(new GrantEntitlementReward(MKUEntitlements.ClericTier1));
        def.addQuest(goldBars);

        DialogueBuilder killDead = DialogueBuilder.hail(
                        "Now that you have joined our efforts, perhaps you would like to [learn|want to learn|I want to learn.] some of " +
                                "our spells, and maybe you can use one of those to help us with a [task|I will assist with your task.].")
                .effectNode("task", "We need someone to go out and cull the dead that walk amongst the living still. Return to me when you've completed",
                        new ObjectiveCompleteEffect("return_to_cleric", "return_to_cleric"))
                .effectNode("want to learn", "Let me see what I can teach you. Talk to me again when you're done.", new OpenLearnAbilitiesEffect());


        Quest return1 = new QuestBuilder("return_to_cleric",
                Component.literal("Return to the Cleric"))
                .autoComplete(true)
                .builderHail("return_to_cleric", Component.literal("Talk to the Cleric again."),
                        cleric,
                        killDead,
                        null
                )
                .reward(new XpReward(50))
                .reward(new FactionReward(50, factionReg.getOrThrow(MKUFactions.SEE_OF_SOLANG_NAME)))
                .quest();
        def.addQuest(return1);

        Quest killDeadObj = new QuestBuilder("kill_dead_1",
                Component.literal("Cull the dead that walk amongst the living."))
                .autoComplete(true)
                .killType("kill_dead_1", EntityTypeTags.UNDEAD, "Undead", 20)
                .reward(new XpReward(250))
                .quest();
        def.addQuest(killDeadObj);

        DialogueBuilder postKillDead = DialogueBuilder.hail("It seems you have a talent for this type of work. " +
                "Those with the strength to [purify the land|How can I help purify the land?] are needed around these parts.")
                .node("purify the land", "The dead walk everywhere, but some places call to the fel spirits that drive them. " +
                        "Not far from here lies such a place: a [tomb|What must I do at this tomb?] built by the [ancients|Who were the ancients?] many, many years ago.")
                .node("ancients", "The Hyborean Empire used to extend to the furthest reaches of these lands. Now only shattered remains of their " +
                        "tombs and cities can be found. Few have ever stepped foot in these ruins as vengeful spirits still haunt them.")
                .effectNode("tomb", "You must go to the tomb and destroy the greater spirits found within. " +
                        "Be cautious for your targets still retain some of the vitality they possessed in life.",
                        new ObjectiveCompleteEffect("return_after_kill_dead_1", "return_after_kill_dead_1"));

        Quest returnAfterKill = new QuestBuilder("return_after_kill_dead_1",
                Component.literal("Return to the Cleric"))
                .autoComplete(true)
                .builderHail("return_after_kill_dead_1", Component.literal("Talk to the Cleric again."),
                        cleric,
                        postKillDead,
                        null
                )
                .reward(new FactionReward(100, factionReg.getOrThrow(MKUFactions.SEE_OF_SOLANG_NAME)))
                .reward(new GrantEntitlementReward(MKUEntitlements.ClericTier2))
                .reward(new XpReward(250))
                .quest();
        def.addQuest(returnAfterKill);

        Quest killDead2 = new QuestBuilder("kill_dead_2",
                Component.literal("Destroy the Greater Dead"))
                .autoComplete(true)
                .killOneOfNotables("kill_greater", tomb, List.of(ancient_king.npcDef, sorcerer_queen.npcDef))
                .killNpc("kill_warriors", MKUltra.id("hyborean_warrior"), 8)
                .killNpc("kill_honor_guard", MKUltra.id("hyborean_honor_guard"), 4)
                .killNpc("kill_sorcerers", MKUltra.id("hyborean_sorcerer"), 4)
                .reward(new XpReward(100))
                .quest();

        def.addQuest(killDead2);

        DialogueBuilder postKill2 = DialogueBuilder
                .hail("It is the solumn duty of a Cleric of Solang to bring His Light into the Dark and drive back the dead with it. " +
                        "You have shown great talent at this task. I hope you will continue to act with faith and fortune.", true);

        Quest afterKill2 = new QuestBuilder("after_kill_2",
                Component.literal("Return to the Cleric"))
                .autoComplete(true)
                .builderHail("return_after_kill_2", Component.literal("Talk to the Cleric again."),
                        cleric, postKill2, null)
                .reward(new FactionReward(200, factionReg.getOrThrow(MKUFactions.SEE_OF_SOLANG_NAME)))
                .reward(new GrantEntitlementReward(MKUEntitlements.ClericTier3))
                .reward(new XpReward(400))
                .quest();

        def.addQuest(afterKill2);

        return def;
    }

    private QuestDefinition generateIntroMageQuest() {
        QuestStructureLocation introCastle = new QuestStructureLocation(UltraStructures.INTRO_CASTLE.location(), "0");
        QuestBuilder.QuestNpc initiate = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("nether_mage_initiate"));
        QuestBuilder.QuestNpc magus = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("imperial_magus"));

        QuestDefinition def = new QuestDefinition(MKUltra.id("nether_mage_intro"));
        def.setRepeatable(false);
        def.setQuestName(Component.literal("Helping the Nether Mage"));

        DialogueBuilder start = DialogueBuilder.hail(
                "Hello, I'm {name}. The [guild|What guild?] sent me out here to research the appearance of this castle. " +
                        "However, I was overwhelmed by zombies and found shelter in this cave. " +
                        "Perhaps you can help me finish [my assignment|assignment|I will help with your assignment.].")
                .node("assignment", "I was sent here to test a new magical " +
                        "staff my master has been working on, but I dropped it when I was [ambushed|You were ambushed?] in the library.")
                .node("ambushed", "{player} will you retrieve my staff from {magus}")
                .node("guild", "The Nether Mage's Guild studies the Fire and Shadow Magics associated " +
                        "with the Nether dimension. We have guild halls all over the place, I'm surprised you haven't heard of us!")
                .context("player", DialogueContexts.PLAYER_NAME_CONTEXT)
                .context("name", DialogueContexts.ENTITY_NAME_CONTEXT)
                .context("magus", magus.getDialogueLink());

        start.build().populateStart(def, "ambushed");

        Quest getStaff = new QuestBuilder("get_staff",
                Component.literal("The Nether Mage Initiate wants you to retrieve a staff from the library."))
                .autoComplete(true)
                .questLootFromNotable("loot_staff", magus, 1.0, 1, Component.literal("The Magic Staff"))

                .quest();
        def.addQuest(getStaff);

        DialogueBuilder killZombies = DialogueBuilder.hail(
                "Thanks for retrieving this staff, you really saved my ass. Will you do one more thing for me? " +
                        "My assignment was to use this staff to [kill some of the zombies|I will kill some of the zombies.] here to test its efficacy.")
                .effectNode("kill some of the zombies", "Great, return to me when you've delivered 10 killing blows with the fireball from the staff.",
                        new ObjectiveCompleteEffect("return_to_initiate", "return_to_initiate"));


        Quest returnToInitiate = new QuestBuilder("return_to_initiate",
                Component.literal("Return to the Initiate with the Magic Staff"))
                .autoComplete(true)
                .builderHail("return_to_initiate", Component.literal("Talk to the Initiate again."),
                        initiate,
                        killZombies,
                        null
                )
                .reward(new MKLootReward(MKUltra.id("burning_staff"),
                        LootSlotManager.MAIN_HAND,
                        Component.translatable("mkultra.quest_reward.receive_item.name", Component.literal("Burning Staff"))))
                .reward(new XpReward(25))
                .quest();
        def.addQuest(returnToInitiate);

        Quest testStaff = new QuestBuilder("test_staff",
                Component.literal("Land Killing Blows with the Fireball Ability granted by the Initiate's Staff"))
                .autoComplete(true)
                .killWithAbility("test_staff", MKUAbilities.FIREBALL.get(), 10)
                .reward(new XpReward(25))
                .quest();
        def.addQuest(testStaff);

        DialogueBuilder complete = DialogueBuilder.hail(
                "Looks like the staff is in working order. You know you weren't half bad at this, you should consider " +
                        "joining [the Guild|guild|What guild?]. In the meantime, I can [teach you|teach me|Will you teach me?] a few spells.",
                true
        );

        Quest finalReturn = new QuestBuilder("test_complete", Component.literal("Return to the Initiate"))
                .autoComplete(true)
                .builderHail("test_complete",
                        Component.literal("Talk to the Initiate"),
                        initiate,
                        complete,
                        null)
                .reward(new GrantEntitlementReward(MKUEntitlements.IntroNetherMageTier1))
                .reward(new XpReward(50))
                .quest();
        def.addQuest(finalReturn);

        return def;
    }

    private MutableComponent text(String literal) {
        return Component.literal(literal);
    }

    private QuestDefinition generateIntroClericQuest() {
        QuestStructureLocation introCastle = new QuestStructureLocation(UltraStructures.INTRO_CASTLE.location(), "0");
        QuestBuilder.QuestNpc acolyte = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("solangian_acolyte"));
        QuestBuilder.QuestNpc apprentice = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("solangian_apprentice"));
        QuestBuilder.QuestNpc magus = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("imperial_magus"));

        QuestDefinition def = new QuestDefinition(MKUltra.id("cleric_intro"));
        def.setRepeatable(false);
        def.setQuestName(Component.literal("A Missing Apprentice"));


        var builder = DialogueBuilder.hail(
                        "I am {name}, sent here under the authority of [Holy See of Solang|the Holy See|Who are the Holy See?] " +
                                "to investigate the appearance of this castle. I hear you are going into the castle; " +
                                "if you're interested,I have a [task|What task?] for you."
                )
                .node("the Holy See", "Our order is dedicated to the worship of the Sun God, Solang. We work to " +
                        "preserve order and prosperity in the realm. This plague of undeath is of great " +
                        "concern to the [Council|the Council?] and we believe that this castle " +
                        "is somehow connected.")
                .node("Council", "The leadership of my order is called the Council of the Nine. They are tasked with overseeing all affairs of the church.")
                .node("task", "While you are exploring the castle, could you search for [my apprentice|apprentice|Where did you last see your apprentice?]? " +
                        "We were ambushed by zombies while investigating the library and had to split up. I made it back but {apprentice} has yet to return.")
                .node("apprentice", "I last saw {apprentice} in the library on the upper floors of the castle.")
                .context("name", DialogueContexts.ENTITY_NAME_CONTEXT)
                .context("apprentice", apprentice.getDialogueLink());
        var result = builder.build();
        result.populateStart(def, "apprentice");

        var apprenticeBuilder = DialogueBuilder.hail(
                "Oh thank goodness, it is good to see a friendly face. One of the zombies chased me into " +
                        "here and I wasn't certain if I'd ever get out. Can you do me [a favor|favor|What favor?]"
        ).effectNode("favor",
                "When we were escaping from the library I accidentally dropped a necklace " +
                "of sentimental value.  I think the {magus} has it. Will you retrieve it for me?",
                new ObjectiveCompleteEffect("talk_to_apprentice", "talk_to_apprentice")
        ).context("magus", magus.getDialogueLink());


        Quest talkToApprentice = new QuestBuilder("talk_to_apprentice",
                Component.literal("You need to find the Apprentice somewhere in the castle. Perhaps near the library.."))
                .autoComplete(true)
                .builderHail("talk_to_apprentice",
                        Component.literal("Talk to the apprentice"),
                        apprentice,
                        apprenticeBuilder,
                        null
                )
                .reward(new XpReward(25))
                .quest();
        def.addQuest(talkToApprentice);

        Quest apprenticeNecklace = new QuestBuilder("loot_necklace",
                text("The Apprentice wants you to retrieve their necklace from a zombie in the library."))
                .autoComplete(true)
                .questLootFromNotable("loot_necklace", magus, 1.0, 1, text("The Apprentice's Necklace"))
                .reward(new XpReward(25))
                .quest();
        def.addQuest(apprenticeNecklace);

        Quest returnToApprentice = new QuestBuilder("return_to_apprentice",
                text("Return the necklace to the Apprentice"))
                .autoComplete(true)
                .simpleHail("return_to_apprentice",
                        text("Talk to the apprentice"),
                        apprentice,
                        String.format("Thank you so much I don't think I could have handled %s on my own. Please let %s know I will return shortly.",
                                magus.getDialogueLink(), acolyte.getDialogueLink()),
                        true,
                        null
                )
                .reward(new XpReward(25))
                .quest();
        def.addQuest(returnToApprentice);

        Quest returnToAcolyte = new QuestBuilder("return_to_acolyte",
                text("Return to the Acolyte and let them know the Apprentice is safe."))
                .autoComplete(true)
                .simpleHail("return_to_acolyte",
                        text("Return to the Acolyte"),
                        acolyte,
                        String.format("I'm glad %s is alright. Thank you for all you've done. For your service, I will bend my order's rules a little and provide you with some training in our healing magics.",
                                acolyte.getDialogueLink()),
                        true,
                        null
                )
                .reward(new XpReward(25))
                .reward(new GrantEntitlementReward(MKUEntitlements.IntroClericTier1))
                .quest();
        def.addQuest(returnToAcolyte);

        return def;
    }

    private QuestDefinition generateTrooperArmorQuest() {
        QuestStructureLocation introCastle = new QuestStructureLocation(UltraStructures.INTRO_CASTLE.location(), "0");
        ResourceLocation greenSmith = MKUltra.id("green_smith");

        QuestDefinition def = new QuestDefinition(MKUltra.id("trooper_armor"));
        def.addRequirement(new HasEntitlementRequirement(MKUEntitlements.GreenKnightTier1));
        def.setRepeatable(true);
        def.setQuestName(text("Salvaged Trooper Armor"));
        def.setMode(QuestDefinition.QuestMode.UNSORTED);
        DialoguePrompt startQuestPrompt = new DialoguePrompt("start_quest", "need some armor",
                "I need some armor", "need some armor.");
        startQuestPrompt.addResponse(new DialogueResponse("start_quest"));
        DialogueNode hailNode = new DialogueNode("hail", String.format("I make armor for the Green Knights. " +
                        "We're running low on supplies but if you can salvage some parts from the zombies in the castle " +
                        "If you %s I should be able to put something together.",
                startQuestPrompt.getPromptEmbed()));
        DialogueNode questStart = new DialogueNode("start_quest",
                String.format("For the full set I will need %s, a %s, a %s, a %s, and a %s.", DialogueUtils.getStackCountItemProvider(new ItemStack(MKUItems.corruptedPigIronPlate.get(), 20)),
                        DialogueUtils.getItemNameProvider(MKUItems.destroyedTrooperHelmet.get()), DialogueUtils.getItemNameProvider(MKUItems.destroyedTrooperLeggings.get()),
                        DialogueUtils.getItemNameProvider(MKUItems.destroyedTrooperChestplate.get()), DialogueUtils.getItemNameProvider(MKUItems.destroyedTrooperBoots.get())));
        def.setupStartQuestResponse(questStart, startQuestPrompt);
        def.addHailResponse(hailNode);


        Quest helmet = new Quest("tradeHelmet", text("The Green Smith needs " +
                "some scrap metal and a helmet from the pigs in the castle."));
        helmet.setAutoComplete(true);
        TradeItemsObjective helmetTrade = new TradeItemsObjective(
                "tradeHelmetObj",
                introCastle,
                greenSmith,
                List.of(
                        new ItemStack(MKUItems.corruptedPigIronPlate.get(), 2),
                        new ItemStack(MKUItems.destroyedTrooperHelmet.get())
                ));
        helmet.addObjective(helmetTrade);
        helmet.addReward(new XpReward(25));
        helmet.addReward(new MKLootReward(MKUltra.id("trooper_knight_armor"),
                LootSlotManager.HEAD,
                Component.translatable("mkultra.quest_reward.receive_item.name", MKUItems.trooperKnightHelmet.get().getDescription())));
        def.addQuest(helmet);

        Quest leggings = new Quest("tradeLeggings", text("The Green Smith needs " +
                "some scrap metal and a pair of leggings from the pigs in the castle."));
        leggings.setAutoComplete(true);
        TradeItemsObjective leggingsTrade = new TradeItemsObjective(
                "tradeHelmetObj",
                introCastle,
                greenSmith,
                List.of(
                        new ItemStack(MKUItems.corruptedPigIronPlate.get(), 6),
                        new ItemStack(MKUItems.destroyedTrooperLeggings.get())
                ));
        leggings.addObjective(leggingsTrade);
        leggings.addReward(new XpReward(25));
        leggings.addReward(new MKLootReward(MKUltra.id("trooper_knight_armor"),
                LootSlotManager.LEGS,
                Component.translatable("mkultra.quest_reward.receive_item.name", MKUItems.trooperKnightLeggings.get().getDescription())));
        def.addQuest(leggings);

        Quest boots = new Quest("tradeBoots", text("The Green Smith needs " +
                "some scrap metal and a pair of boots from the pigs in the castle."));
        boots.setAutoComplete(true);
        TradeItemsObjective bootTrade = new TradeItemsObjective(
                "tradeLeggingsObj",
                introCastle,
                greenSmith,
                List.of(
                        new ItemStack(MKUItems.corruptedPigIronPlate.get(), 4),
                        new ItemStack(MKUItems.destroyedTrooperBoots.get())
                ));
        boots.addObjective(bootTrade);
        boots.addReward(new XpReward(25));
        boots.addReward(new MKLootReward(MKUltra.id("trooper_knight_armor"),
                LootSlotManager.FEET,
                Component.translatable("mkultra.quest_reward.receive_item.name", MKUItems.trooperKnightBoots.get().getDescription())));
        def.addQuest(boots);

        Quest chestplate = new Quest("tradeChestplate", text("The Green Smith needs " +
                "some scrap metal and the chestplate from the pigs in the castle."));
        chestplate.setAutoComplete(true);
        TradeItemsObjective chestplateTrade = new TradeItemsObjective(
                "tradeChestObj",
                introCastle,
                greenSmith,
                List.of(
                        new ItemStack(MKUItems.corruptedPigIronPlate.get(), 8),
                        new ItemStack(MKUItems.destroyedTrooperChestplate.get())
                ));
        chestplate.addObjective(chestplateTrade);
        chestplate.addReward(new XpReward(25));
        chestplate.addReward(new MKLootReward(MKUltra.id("trooper_knight_armor"),
                LootSlotManager.CHEST,
                Component.translatable("mkultra.quest_reward.receive_item.name", MKUItems.trooperKnightChestplate.get().getDescription())));
        def.addQuest(chestplate);

        return def;
    }

    private QuestDefinition generateIntroQuest() {

        QuestStructureLocation introCastle = new QuestStructureLocation(UltraStructures.INTRO_CASTLE.location(), "0");
        QuestBuilder.QuestNpc greenLady = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("green_lady"));
        QuestBuilder.QuestNpc piglinCaptain = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("trooper_captain"));
        QuestBuilder.QuestNpc greenSmith = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("green_smith"));
        QuestBuilder.QuestNpc forlornGhost = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("forlorn_ghost"));
        QuestBuilder.QuestNpc burningRevenant = new QuestBuilder.QuestNpc(introCastle, MKUltra.id("burning_skeleton"));

        QuestDefinition def = new QuestDefinition(MKUltra.id("intro_quest"));
        def.setQuestName(text("The Green Knights"));

        DialogueBuilder hail = DialogueBuilder.hail(
                "Hail and well met. You're lucky we were able to grab you" +
                        " before your soul drifted too far into the aether. [What are you doing|don't know|I don't know] " +
                        "in an archival zone?")
                .node("don't know", "This world is on the verge of deletion, the dead rise from " +
                        "the ground everywhere, there may still be time to save it if we act now. " +
                        "We're in need of another hero: go talk to our smith and get equipped.");

        hail.build().populateStart(def, "don't know");

        Quest talk1 = new QuestBuilder("talk_to_smith",
                text("The Green Lady wants you to go talk to the smith and equip yourself for an unknown task."))
                .autoComplete(true)
                .simpleHail("talk_to_smith",
                        text("Talk to the smith"),
                        greenSmith,
                        "We ain't got much left after the crash. " +
                                "Check that chest over there we got a few things. You can use my crafting table as well. " +
                                "Talk to me again when you have made a weapon.",
                        true,
                        null
                )
                .reward(new XpReward(25))
                .quest();
        def.addQuest(talk1);


        Quest lootSmithChest = new QuestBuilder("equip_yourself", text("The Green Smith points you towards a chest in his workshop."))
                .autoComplete(true)
                .lootChest("loot_chest", text("Loot the smith's chest"), introCastle, "intro_chest",
                        new ItemStack(Blocks.COBBLESTONE, 20),
                        new ItemStack(Blocks.OAK_PLANKS, 20),
                        new ItemStack(Items.STRING, 10),
                        new ItemStack(Items.LEATHER, 40),
                        new ItemStack(Items.COAL, 10),
                        new ItemStack(Items.FLINT, 10),
                        new ItemStack(Items.PORKCHOP, 10)
                )
                .reward(new XpReward(25))
                .quest();
        def.addQuest(lootSmithChest);


        Quest returnToSmith = new QuestBuilder("return_to_smith", text("Use the Green Smith's supplies to craft your desired weapon and perhaps some armor for the battle ahead."))
                .autoComplete(true)
                .hailWithCondition("return_to_smith",
                        text("Talk to the Green Smith with a weapon in your hand."),
                        greenSmith,
                        "Great, but you're going to need more than just a sharp rock where we're going. " +
                                "Go back and talk to the Green Lady, ask her about learning to develop your magical talents.",
                        "Come back to me with a weapon in your hand.",
                        new HasWeaponInHandCondition(),
                        null
                )
                .reward(new XpReward(25))
                .quest();
        def.addQuest(returnToSmith);


        Quest greenLadyTrainTalent = new QuestBuilder("green_lady_talent",
                text("Talk to the Green Lady to learn more about developing your magical abilities"))
                .autoComplete(true)
                .simpleHail(
                        "green_lady_talent",
                        text("Talk to the Green Lady about the talent system."),
                        greenLady,
                        "We can help you awaken your magical gifts, the first step is learning how " +
                                "to train your talents. You should have gained a talent point upon initiating this conversation. " +
                                "Open your player screen and go to the talent section, train any of the first talents in " +
                                "order to unlock your first ability slot. Talk to me again when you have finished this.",
                        true,
                        null
                )
                .reward(new XpReward(25))
                .quest();
        def.addQuest(greenLadyTrainTalent);


        DialogueBuilder training = DialogueBuilder.hailWithCondition(
                "Alright you're now [ready to learn|want to learn|I want to learn] your first ability.",
                "Come back to me when you have spent your first talent point.",
                new HasSpentTalentPointsCondition(1)
        ).effectNode("want to learn", "Let me see what I can teach you. Talk to me again when you're done",
                new OpenLearnAbilitiesEffect());

        Quest returnToGreenLady = new QuestBuilder("return_to_green_lady",
                text("The Green Lady wants you to learn about spending talent points."))
                .autoComplete(true)
                .builderHail("return_to_green_lady",
                        text("Talk to the Green Lady after training a talent."),
                        greenLady,
                        training,
                        null
                )
                .reward(new XpReward(50))
                .reward(new GrantEntitlementReward(MKUEntitlements.GreenKnightTier1))
                .quest();
        def.addQuest(returnToGreenLady);


        DialogueBuilder ability = DialogueBuilder.hailWithCondition(
                "Now we must test your mettle in combat. " +
                        "Go kill some of the zombies on the first floor to try out your new magic, and don't forget you can always return to me to learn more.",
                "Come back to me once you've [learned|want to learn|I want to learn] one of our abilities.",
                new HasTrainedAbilitiesCondition(false, MKUAbilities.SKIN_LIKE_WOOD.getId(), MKUAbilities.NATURES_REMEDY.getId())
        ).effectNode("want to learn", "Let me see what I can teach you. Talk to me again when you're done.",
                new OpenLearnAbilitiesEffect());

        Quest afterAbility = new QuestBuilder("after_ability",
                text("Talk to the Green Lady and learn your first ability, then speak to her again."))
                .autoComplete(true)
                .builderHail("after_green_lady",
                        text("Talk to the Green Lady after learning your first ability."),
                        greenLady,
                        ability,
                        null)
                .reward(new XpReward(50))
                .quest();
        def.addQuest(afterAbility);


        Quest killQuest = new QuestBuilder("first_kill",
                text("The Green Lady wants you to clear out some of the zombies on the first floor of the castle"))
                .autoComplete(true)
                .killNpc("kill_zombies", MKUltra.id("decaying_piglin"), 4)
                .killNpc("kill_archers", MKUltra.id("decaying_piglin_archer"), 4)
                .hailWithObjectives("after_kill",
                        text("Talk to the Green Lady after completing the other objectives."),
                        greenLady,
                        String.format("Your skills have not gone unnoticed. " +
                                        "The dead rise everywhere, to cull the damned is a blessed pursuit. " +
                                        "Would you be willing to go back into that cursed hall and destroy %s.",
                                piglinCaptain.getDialogueLink()),
                        "Come back to me after you've proven yourself.",
                        Arrays.asList("kill_zombies", "kill_archers"),
                        null)
                .reward(new XpReward(50))
                .quest();
        def.addQuest(killQuest);


        Quest killCaptain = new QuestBuilder("kill_captain", text("The Green Lady wants you to find and kill the Piglin Captain"))
                .autoComplete(true)
                .killNotable("kill_captain", piglinCaptain)
                .hailWithObjectives(
                        "after_kill_captain",
                        text("Talk to the Green Lady after completing the other objectives."),
                        greenLady,
                        "I need you to return to the castle and delve even deeper. I know not why it appeared here at this time, but I do sense a residual life force beneath the castle. " +
                                "Perhaps this spirit will be amiable to conversation. Go find it.",
                        String.format("Come back to me after you've taken care of %s.", piglinCaptain.getDialogueLink()),
                        Collections.singletonList("kill_captain"),
                        null
                )
                .reward(new XpReward(100))
                .reward(new GrantEntitlementReward(MKUEntitlements.GreenKnightTier2))
                .quest();
        def.addQuest(killCaptain);


        DialogueBuilder ghostBuilder = DialogueBuilder.hail("Those [dimension-hopping crusaders|crusaders|What crusaders?] sent you after me didn't they?")
                .node("crusaders", "They call themselves the Green Knights, we had them in [my time|your time|When was your time?] as well. " +
                        "Serve some orc called the [Green Lady|What do you know about the Green Lady?]. They're hyper-focused on seeking out and destroying corruption " +
                        "throughout the known planes; think we're all in [grave peril|What grave peril?] and so on.")
                .node("grave peril", "Oh the same old hogwash about the world being overrun by the undead and then deleted. " +
                        "In the Hyborean religion we believe the world, which is a giant cube, travels around in the dice bag of an interplanar vagrant." +
                        " Obviously the world will end when aforesaid villain abandons, forgets, or perhaps loses us in a game of chance to an even iller-suited caretaker.")
                .node("Green Lady", "There's always only one Green Lady. " +
                        "I don't know if it's always the same one. The Green Lady is in charge, whatever that means. The GK's are a secretive bunch. I never dealt with them when I was alive.")
                .node("your time", "Best I can tell, that was 500 or so years ago. " +
                        "This castle dates back to the [Piglin Empire|the Piglin Empire?] of my time.")
                .node("Piglin Empire", "Those pigs just can't stop killing and stealing. They show up every hundred years or so. " +
                        "I died here in an early [attempt|What happened during the attempt?] at banishing a Piglin Castle back to their home-plane.")
                .node("attempt", "I was mortally wounded when the time came, and something went wrong. My partner and I were unable to complete the ritual. " +
                        "My body was destroyed immediately, leaving my soul bound to the castle grounds. My partner's spirit was shorn from their body as the spell completed, " +
                        "leaving only a [soulless husk|a soulless husk?] to stalk the depths of this cursed castle.")
                .effectNode("soulless husk", "The skeleton that stands ever-burning in the chamber beyond. Destroy this revenant and return to your Green Lady. Leave me to infinity.",
                        new ObjectiveCompleteEffect("talk_to_ghost", "talk_to_ghost"));


        Quest talkToGhost = new QuestBuilder("talk_to_ghost", text("The Green Lady wants you to seek out a spirit in the depths."))
                .autoComplete(true)
                .builderHail("talk_to_ghost", text("Find the spirit in the castle."), forlornGhost,
                       ghostBuilder,
                        null
                )
                .reward(new XpReward(25))
                .quest();
        def.addQuest(talkToGhost);


        Quest killBurning = new QuestBuilder("kill_burning", text("The Forlorn Ghost has asked you you to kill the Burning Revenant"))
                .autoComplete(true)
                .killNotable("kill_burning", burningRevenant)
                .hailWithObjectives(
                        "after_kill_burning",
                        text("Return to the Green Lady after completing the other objectives."),
                        greenLady,
                        "Good: it is done. Our order is dedicated to cleansing this land. You are welcome to stay here and learn of our ways or go as you please.",
                        String.format("Has the castle been cleansed, is the %s dead?", burningRevenant.getDialogueLink()),
                        Collections.singletonList("kill_burning"),
                        null
                )
                .reward(new XpReward(100))
                .reward(new GrantEntitlementReward(MKUEntitlements.GreenKnightTier3))
                .reward(new TalentTreeReward(MKUltra.id("green_knight_talents")))
                .quest();
        def.addQuest(killBurning);


        return def;
    }
}
