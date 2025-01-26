package com.chaosbuffalo.mkultra.data.generators.npc;


import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.data.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.*;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MKUNpcProvider extends NpcDefinitionProvider {

    public MKUNpcProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(generator, lookupProvider, MKUltra.MODID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                writeDefinition(GreenKnightNpcs.generateGreenLady(), cache),
                writeDefinition(GreenKnightNpcs.generateGreenLadyGuard1(), cache),
                writeDefinition(GreenKnightNpcs.generateGreenLadyGuard2(), cache),
                writeDefinition(GreenKnightNpcs.generateGreenSmith(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanWarrior(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanHonorGuard(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanArcher(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanSorcerer(), cache),
                writeDefinition(HyboreanNpcs.generateAncientKing(), cache),
                writeDefinition(HyboreanNpcs.generateHyboreanSorcererQueen(), cache),
                writeDefinition(IntroCastleNpcs.generateCrumblingTrooper(), cache),
                writeDefinition(generateCrumblingTrooperMage(), cache),
                writeDefinition(generateDecayingZombieArcher(), cache),
                writeDefinition(generateDecayingZombiePiglin(), cache),
                writeDefinition(generateImperialMagus(), cache),
                writeDefinition(generateTrooperCaptain(), cache),
                writeDefinition(generateTrooperExecution(), cache),
                writeDefinition(IntroCastleNpcs.generateSkeletalTrooperMage(), cache),
                writeDefinition(generateBurningSkeleton(), cache),
                writeDefinition(generateClericAcolyte(), cache),
                writeDefinition(generateClericApprentice(), cache),
                writeDefinition(generateForlornGhost(), cache),
                writeDefinition(generateNetherMageInitiate(), cache),
                writeDefinition(generateTempleGuard(), cache),
                writeDefinition(generateTempleGuard2(), cache),
                writeDefinition(generateCleric(), cache),
                writeDefinition(generateNecrotideCultistAcolyte(), cache),
                writeDefinition(NecrotideNpcs.generateNecrotideCultist(), cache),
                writeDefinition(generateSkeletalLock(), cache),
                writeDefinition(generateNecrotideGolem(), cache),
                writeDefinition(generateNecrotideSkeletalArcher(), cache),
                writeDefinition(generateNecrotideSkeletalWarrior(), cache),
                writeDefinition(generateSeawovenSkeleton(), cache),
                writeDefinition(generateSeawovenWretch(), cache),
                writeDefinition(generateAncientPriestGhost(), cache),
                writeDefinition(generateAncientCardinal(), cache),
                writeDefinition(generateGhostApprentice(), cache)
        );
    }

    private NpcDefinition generateSeawovenWretch() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("seawoven_wretch"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(MKUSkeletons.SEAWOVEN_WRTECH_NAME));
        def.addOption(new MKSizeOption(0.92f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 35.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 35.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Seawoven Wretch"));
        def.addOption(new AbilitiesOption().withAbilityOption(MKUAbilities.FROZEN_GRASP.get(), 1, 1.0));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.NECROMANCER));
        return def;
    }

    private NpcDefinition generateSeawovenSkeleton() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("seawoven_skeleton"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(MKUSkeletons.SEAWOVEN_NAME));
        def.addOption(new MKSizeOption(0.98f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 45.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 45.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Seawoven Skeleton"));
        def.addOption(new AbilitiesOption().withAbilityOption(MKUAbilities.SEAFURY.get(), 1, 1.0));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.NECROMANCER));
        ResourceLocation lootTierName = MKUltra.id("seawoven_skeleton");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.ITEMS.getName(), lootTierName, 1.0))
                .withDropChances(1)
                .withNoLootChance(0.25)
                .withNoLootIncrease(0.25)
        );
        return def;
    }

    private NpcDefinition generateSkeletalLock() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("skeletal_lock"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new RenderGroupOption(MKUSkeletons.BASIC_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Skeletal Lock"));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.SHADOW_BOLT.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SHADOW_PULSE.get(), 2, 1.0)
        );
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        def.addOption(new NotableOption());
        return def;
    }

    private NpcDefinition generateNecrotideGolem() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_golem"),
                MKUEntities.GOLEM_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new RenderGroupOption(MKUGolems.NECROTIDE_GOLEM_NAME));
        def.addOption(new MKSizeOption(1.25f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 5.0))
        );
        def.addOption(new NameOption("A Necrotide Construction"));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.SHADOW_BOLT.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.NECROTIDE_GOLEM_BEAM.get(), 1, 1.0)
        );
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.NECROMANCER));
        def.addOption(new NotableOption());
        ResourceLocation lootTierName = MKUltra.id("necrotide_golem");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.RINGS.getName(), lootTierName, 3.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.HANDS.getName(), lootTierName, 1.0))
                .withDropChances(1)
                .withNoLootChance(0.0)
                .withNoLootIncrease(0.0));
        return def;
    }

    private NpcDefinition generateNecrotideCultistAcolyte() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_acolyte"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.NECROTIDE_CULTIST_1_NAME));
        def.addOption(new MKSizeOption(0.95f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Necrotide Acolyte"));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.SHADOW_BOLT.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.DROWN.get(), 2, 0.5)
                .withAbilityOption(MKUAbilities.SHADOW_PULSE.get(), 3, 0.5)
        );
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        def.addOption(new FactionBattlecryOption());
        return def;
    }

    private NpcDefinition generateNecrotideSkeletalWarrior() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_skeletal_warrior"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new RenderGroupOption(MKUSkeletons.BASIC_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 65.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 65.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("Skeleton Warrior"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:battleaxe_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:spear_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:greatsword_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:longsword_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:warhammer_iron"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.WARRIOR));
        return def;
    }

    private NpcDefinition generateNecrotideSkeletalArcher() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_skeletal_archer"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new MKSizeOption(0.95f));
        def.addOption(new RenderGroupOption(MKUSkeletons.HYBOREAN_ARCHER_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 30.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 30.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 1.0))
        );
        def.addOption(new NameOption("Skeleton Archer"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:longbow_iron"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.ARCHER));
        return def;
    }


    private NpcDefinition generateClericAcolyte() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_acolyte"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.CLERIC_1_NAME));
        def.addOption(new MKSizeOption(1.05f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 150.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 150.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.5))
        );
        def.addOption(new FactionNameOption().setTitle("Acolyte"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.GALVANIZE.get(), 3, 1.0)
        );
        def.addOption(new DialogueOption(MKUltra.id("intro_cleric_acolyte")));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:mace_iron"))), 1.0, 0.0f));
        def.addOption(new QuestOfferingOption(MKUltra.id("cleric_intro")));
        def.addOption(new AbilityTrainingOption()
                .withTrainingOption(MKUAbilities.HEAL, new HasEntitlementRequirement(MKUEntitlements.IntroClericTier1.get()))
                .withTrainingOption(MKUAbilities.SMITE, new HasEntitlementRequirement(MKUEntitlements.IntroClericTier1.get()))
        );
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.CLERIC));
        return def;
    }

    private NpcDefinition generateCleric() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_cleric"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.CLERIC_1_NAME));
        def.addOption(new MKSizeOption(1.05f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 5.0))
        );
        def.addOption(new FactionNameOption().setTitle("Cleric"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.GALVANIZE.get(), 3, 1.0)
                .withAbilityOption(MKUAbilities.POWER_WORD_SUMMON.get(), 4, 1.0)
                .withAbilityOption(MKUAbilities.INSPIRE.get(), 5, 1.0)
        );
        def.addOption(new DialogueOption(MKUltra.id("cleric_default")));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:mace_gold"))), 1.0, 0.0f));
        def.addOption(new QuestOfferingOption(MKUltra.id("cleric_unlock_chain")));
        def.addOption(new AbilityTrainingOption()
                .withTrainingOption(MKUAbilities.HEAL, new HasEntitlementRequirement(MKUEntitlements.ClericTier1.get()))
                .withTrainingOption(MKUAbilities.SMITE, new HasEntitlementRequirement(MKUEntitlements.ClericTier1.get()))
                .withTrainingOption(MKUAbilities.GALVANIZE, new HasEntitlementRequirement(MKUEntitlements.ClericTier2.get()))
                .withTrainingOption(MKUAbilities.POWER_WORD_SUMMON, new HasEntitlementRequirement(MKUEntitlements.ClericTier2.get()))
                .withTrainingOption(MKUAbilities.INSPIRE, new HasEntitlementRequirement(MKUEntitlements.ClericTier3.get()))
        );
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.CLERIC));
        return def;
    }

    private NpcDefinition generateForlornGhost() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("forlorn_ghost"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.GHOST_1_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new NameOption("Forlorn Ghost"));
        def.addOption(new NotableOption());
        def.addOption(new GhostOption().setGhostTranslucency(0.7f));
//        def.addOption(new AbilitiesOption()
//                .withAbilityOption(HealAbility.INSTANCE, 1, 1.0)
//                .withAbilityOption(SmiteAbility.INSTANCE, 2, 1.0)
//        );
//        EquipmentOption equipOption = new EquipmentOption();
//        equipOption.addItemChoice(EquipmentSlotType.MAINHAND,
//                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
//                        ResourceLocation.parse("mkweapons:mace_iron"))), 1.0, 0.0f));
//        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.FIGHTER));
        return def;
    }

    private NpcDefinition generateGhostApprentice() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("ancient_apprentice_ghost"),
                MKUEntities.HUMAN_GHOST_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.GHOSTS_OF_HYBORIA_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.GHOST_LOOK_CLEAN_SHORT_NAME));
        def.addOption(new MKSizeOption(0.92f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new FactionNameOption().setTitle("Apprentice"));
        def.addOption(new GhostOption().setGhostTranslucency(0.7f));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
        );
        def.addOption(new NotableOption());
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.CHEST,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestChestplate.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.LEGS,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestLeggings.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.FEET,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestBoots.get()), 1.0, 0.05f));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.PALADIN));
        def.addOption(equipOption);
        return def;
    }


    private NpcDefinition generateAncientPriestGhost() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("ancient_priest_ghost"),
                MKUEntities.HUMAN_GHOST_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(MKUHumans.GHOST_LOOK_CLEAN_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new NameOption("An Ancient Priest"));
        def.addOption(new GhostOption().setGhostTranslucency(0.7f));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 3, 0.5)
        );
//        EquipmentOption equipOption = new EquipmentOption();
//        equipOption.addItemChoice(EquipmentSlotType.MAINHAND,
//                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
//                        ResourceLocation.parse("mkweapons:mace_iron"))), 1.0, 0.0f));
//        def.addOption(equipOption);
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.HEAD,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestHelmet.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.CHEST,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestChestplate.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.LEGS,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestLeggings.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.FEET,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestBoots.get()), 1.0, 0.05f));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.CLERIC));
        def.addOption(equipOption);
        return def;
    }

    private NpcDefinition generateAncientCardinal() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("ancient_cardinal"),
                MKUEntities.HUMAN_GHOST_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new MKSizeOption(1.2f));
        def.addOption(new RenderGroupOption(MKUHumans.GHOST_LOOK_CLEAN_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 350.0))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ARMOR, 20.0))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ATTACK_DAMAGE, 6.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.HOLY_RESISTANCE, 1.25))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 350.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 6.0))
        );
        def.addOption(new NameOption("Ancient Cardinal"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:warhammer_gold"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.HEAD,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalHelmet.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.CHEST,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalChestplate.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.LEGS,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalLeggings.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.FEET,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalBoots.get()), 1.0, 0.05f));
        def.addOption(equipOption);
        def.addOption(new NotableOption());
        def.addOption(new BossStageOption()
                        .withStage(new BossStage()
                                        .withOption(new TempAbilitiesOption()
                                                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 1, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD_SHOTGUN.get(), 2, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 3, 1.0))
//                        .withOption(new ParticleEffectsOption().withEffects(Collections.singletonList(
//                                new BoneEffectInstance(UUID.fromString("3e7496f1-f5bf-45e6-b8e5-64192633ae9f"),
//                                        BipedSkeleton.HEAD_BONE_NAME, MKUltra.id("flame_wave_casting")))))
                        )
                        .withStage(new BossStage()
                                        .withOption(new TempAbilitiesOption()
                                                .withAbilityOption(MKUAbilities.HOLY_FIRE_FLURRY.get(), 1, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 2, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD_BURST.get(), 3, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD_SHOTGUN.get(), 4, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 5, 1.0))
//                        .withOption(new ParticleEffectsOption().withEffects(Collections.singletonList(
//                                new BoneEffectInstance(UUID.fromString("e45696e1-ddb1-4709-bc29-1733ee1bced9"),
//                                BipedSkeleton.HEAD_BONE_NAME, MKUltra.id("flame_wave_casting")))))
                                        .withParticleMode(BossStage.ParticleMode.LINE_HEIGHT)
                                        .withTransitionParticles(MKUltra.id("wrath_skeleton_transition"))
                                        .withTransitionSound(MKUSounds.spell_holy_9.getId())
                        )
        );
        def.addOption(new ExperienceOption(75));
//        def.addOption(new ParticleEffectsOption(List.of(
//                new BoneEffectInstance(UUID.fromString("3e7496f1-f5bf-45e6-b8e5-64192633ae9f"),
//                        MKUltra.id("burning_skeleton_head"), BipedSkeleton.HEAD_BONE_NAME)
//        )));
//        ResourceLocation lootTierName = MKUltra.id("burning_skeleton");
//        def.addOption(new ExtraLootOption().withLootOptions(new LootOptionEntry(LootSlotManager.MAIN_HAND.getName(), lootTierName, 1.0))
//                .withLootOptions(new LootOptionEntry(LootSlotManager.RINGS.getName(), lootTierName, 3.0))
//                .withLootOptions(new LootOptionEntry(LootSlotManager.EARRINGS.getName(), lootTierName, 2.0))
//                .withDropChances(1)
//                .withNoLootChance(0.1)
//                .withNoLootIncrease(0.0));

        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        return def;
    }


    private NpcDefinition generateTempleGuard2() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_temple_guard_2"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.TEMPLE_GUARD_2_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 300.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 300.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 4.0))
        );
        def.addOption(new FactionNameOption().setTitle("Temple Guard"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.SEVER_TENDON.get(), 3, 1.0)
        );
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:spear_gold"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.PALADIN));
        return def;
    }

    private NpcDefinition generateTempleGuard() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_temple_guard"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.TEMPLE_GUARD_1_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 250.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 250.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new FactionNameOption().setTitle("Temple Guard"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
        );
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:spear_gold"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.PALADIN));
        return def;
    }

    private NpcDefinition generateClericApprentice() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_apprentice"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.CLERIC_2_NAME));
        def.addOption(new MKSizeOption(0.85f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.5))
        );
        def.addOption(new FactionNameOption().setTitle("Apprentice"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
        );
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:mace_iron"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.CLERIC));
        return def;
    }

    private NpcDefinition generateNetherMageInitiate() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("nether_mage_initiate"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NETHER_MAGE_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.NETHER_MAGE_1_NAME));
        def.addOption(new MKSizeOption(0.90f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 150.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 150.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new FactionNameOption().setTitle("Initiate"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.EMBER.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.FIREBALL.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.FLAME_WAVE.get(), 3, 1.0)
        );
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:staff_wood"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(new AbilityTrainingOption()
                .withTrainingOption(MKUAbilities.EMBER, new HasEntitlementRequirement(MKUEntitlements.IntroNetherMageTier1.get()))
                .withTrainingOption(MKUAbilities.FIRE_ARMOR, new HasEntitlementRequirement(MKUEntitlements.IntroNetherMageTier1.get()))
        );
        def.addOption(new DialogueOption(MKUltra.id("intro_nether_mage_initiate")));
        def.addOption(new QuestOfferingOption(MKUltra.id("nether_mage_intro")));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        return def;
    }

    private NpcDefinition generateBurningSkeleton() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("burning_skeleton"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.HYBOREAN_DEAD_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new RenderGroupOption(MKUSkeletons.BURNING_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 250.0))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ARMOR, 10.0))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ATTACK_DAMAGE, 5.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.BLEED_RESISTANCE, 1.25))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 250.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("Burning Revenant"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:dagger_stone"))), 1.0, 0.0f));
        def.addOption(new NotableOption());
        def.addOption(equipOption);
        def.addOption(new BossStageOption()
                        .withStage(new BossStage()
                                        .withOption(new TempAbilitiesOption()
                                                .withAbilityOption(MKUAbilities.FIRE_ARMOR.get(), 3, 1.0)
                                                .withAbilityOption(MKUAbilities.FIREBALL.get(), 2, 1.0)
                                                .withAbilityOption(MKUAbilities.WRATH_BEAM.get(), 1, 1.0))
//                        .withOption(new ParticleEffectsOption().withEffects(Collections.singletonList(
//                                new BoneEffectInstance(UUID.fromString("3e7496f1-f5bf-45e6-b8e5-64192633ae9f"),
//                                        BipedSkeleton.HEAD_BONE_NAME, MKUltra.id("flame_wave_casting")))))
                        )
                        .withStage(new BossStage()
                                        .withOption(new TempAbilitiesOption()
                                                .withAbilityOption(MKUAbilities.FIREBALL.get(), 3, 1.0)
                                                .withAbilityOption(MKUAbilities.WRATH_BEAM_FLURRY.get(), 1, 1.0))
//                        .withOption(new ParticleEffectsOption().withEffects(Collections.singletonList(
//                                new BoneEffectInstance(UUID.fromString("e45696e1-ddb1-4709-bc29-1733ee1bced9"),
//                                BipedSkeleton.HEAD_BONE_NAME, MKUltra.id("flame_wave_casting")))))
                                        .withParticleMode(BossStage.ParticleMode.LINE_HEIGHT)
                                        .withTransitionParticles(MKUltra.id("wrath_skeleton_transition"))
                                        .withTransitionSound(MKUSounds.spell_dark_8.getId())
                        )
        );
        def.addOption(new ExperienceOption(50));
        def.addOption(new ParticleEffectsOption(List.of(
                new BoneEffectInstance(UUID.fromString("3e7496f1-f5bf-45e6-b8e5-64192633ae9f"),
                        MKUltra.id("burning_skeleton_head"), BipedSkeleton.HEAD_BONE_NAME)
        )));
        ResourceLocation lootTierName = MKUltra.id("burning_skeleton");
        def.addOption(new ExtraLootOption().withLootOptions(new LootOptionEntry(LootSlotManager.MAIN_HAND.getName(), lootTierName, 1.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.RINGS.getName(), lootTierName, 3.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.EARRINGS.getName(), lootTierName, 2.0))
                .withDropChances(1)
                .withNoLootChance(0.1)
                .withNoLootIncrease(0.0));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        return def;
    }

    private NpcDefinition generateDecayingZombiePiglin() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("decaying_piglin"),
                MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.IMPERIAL_DEAD_NAME));
        def.addOption(new MKSizeOption(0.9f));
        def.addOption(new RenderGroupOption(MKUPiglins.ZOMBIE_PIG_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 20.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 20.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 1.0))
        );
        def.addOption(new NameOption("Decaying Zombie"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:dagger_stone"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(ItemStack.EMPTY, 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(new ExperienceOption(5));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.FIGHTER));
        return def;
    }

    private NpcDefinition generateDecayingZombieArcher() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("decaying_piglin_archer"),
                MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.IMPERIAL_DEAD_NAME));
        def.addOption(new MKSizeOption(0.85f));
        def.addOption(new RenderGroupOption(MKUPiglins.ZOMBIE_PIG_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 15.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 15.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 1.0))
        );
        def.addOption(new NameOption("Shambling Archer"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(Items.BOW), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(new ExperienceOption(5));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.ARCHER));
        return def;
    }

    private NpcDefinition generateCrumblingTrooperMage() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("crumbling_trooper_mage"),
                MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.IMPERIAL_DEAD_NAME));
        def.addOption(new MKSizeOption(0.95f));
        def.addOption(new RenderGroupOption(MKUPiglins.ZOMBIE_PIG_MAGUS_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 30.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 30.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 1.0))
        );
        def.addOption(new NameOption("Crumbling Trooper Mage"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:dagger_stone"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.FIREBALL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.EMBER.get(), 2, 0.5)
        );
        ResourceLocation lootTierName = MKUltra.id("zombie_trooper");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.ITEMS.getName(), lootTierName, 1.0))
                .withDropChances(2)
                .withNoLootChance(0.1)
                .withNoLootIncrease(0.25)
        );
        def.addOption(new ExperienceOption(10));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        return def;
    }

    private NpcDefinition generateTrooperExecution() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("trooper_executioner"),
                MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.IMPERIAL_DEAD_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new RenderGroupOption(MKUPiglins.DESTROYED_SKELETAL_TROOPER_NAME));
        def.addOption(new MKComboSettingsOption().setComboDelay(20).setComboCount(3));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 180.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 180.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("Trooper Executioner"));
        def.addOption(new LungeSpeedOption(0.75));
        def.addOption(new NotableOption());
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:battleaxe_stone"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.FURIOUS_BROODING.get(), 3, 1.0)
                .withAbilityOption(MKUAbilities.WHIRLWIND_BLADES.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.YANK.get(), 2, 1.0)
        );
        ResourceLocation lootTierName = MKUltra.id("trooper_executioner");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.MAIN_HAND.getName(), lootTierName, 1.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.RINGS.getName(), lootTierName, 3.0))
                .withDropChances(2)
                .withNoLootChance(0.2)
                .withNoLootIncrease(0.25)
        );
        def.addOption(new ExperienceOption(20));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.FIGHTER));
        return def;
    }

    private NpcDefinition generateImperialMagus() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("imperial_magus"),
                MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.IMPERIAL_DEAD_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new RenderGroupOption(MKUPiglins.SKELETAL_MAGE_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 150.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 150.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new FactionNameOption().setTitle("Imperial Magus").setHasLastName(true));
        def.addOption(new NotableOption());
        def.addOption(new LungeSpeedOption(0.5));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:mace_stone"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.FIREBALL.get(), 3, 1.0)
                .withAbilityOption(MKUAbilities.FLAME_WAVE.get(), 6, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 4, 1.0)
                .withAbilityOption(MKUAbilities.FIRE_ARMOR.get(), 5, 1.0)
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.NATURES_REMEDY.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.POWER_WORD_SUMMON.get(), 7, 1.0)
        );
        ResourceLocation lootTierName = MKUltra.id("trooper_magus");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.MAIN_HAND.getName(), lootTierName, 1.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.RINGS.getName(), lootTierName, 2.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.EARRINGS.getName(), lootTierName, 1.0))
                .withDropChances(2)
                .withNoLootChance(0.2)
                .withNoLootIncrease(0.25)
        );
        def.addOption(new ExperienceOption(25));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        return def;
    }

    private NpcDefinition generateTrooperCaptain() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("trooper_captain"),
                MKUEntities.ZOMBIFIED_PIGLIN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.IMPERIAL_DEAD_NAME));
        def.addOption(new MKSizeOption(1.2f));
        def.addOption(new RenderGroupOption(MKUPiglins.SKELETAL_TROOPER_NAME));
        def.addOption(new MKComboSettingsOption().setComboDelay(10).setComboCount(3));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new FactionNameOption().setTitle("Captain").setHasLastName(true));
        def.addOption(new LungeSpeedOption(1.0));
        def.addOption(new NotableOption());
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:greatsword_stone"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:katana_stone"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:warhammer_stone"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.FURIOUS_BROODING.get(), 3, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 1, 1.0)
        );
        ResourceLocation lootTierName = MKUltra.id("trooper_captain");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.MAIN_HAND.getName(), lootTierName, 1.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.EARRINGS.getName(), lootTierName, 3.0))
                .withDropChances(2)
                .withNoLootChance(0.2)
                .withNoLootIncrease(0.25)
        );
        def.addOption(new ExperienceOption(25));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.PALADIN));
        return def;
    }


    @Override
    public String getName() {
        return "MKU NPC GEN";
    }
}
