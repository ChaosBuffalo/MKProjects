package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUPiglins;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.UUID;

public class IntroCastleNpcs {
    static NpcDefinition generateCrumblingTrooper() {
        ResourceLocation lootTierName = MKUltra.id("zombie_trooper");
        return new NpcDefinitionBuilder(MKUltra.id("crumbling_trooper"))
                .type(MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.1f)
                .renderGroup(MKUPiglins.ZOMBIE_PIG_TROOPER_NAME)
                .name("Crumbling Trooper")
                .health(50)
                .mana(50)
                .manaRegen(1.0)
                .lungeSpeed(0.35)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.LONGSWORD_TYPE).get())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.WARHAMMER_TYPE).get())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.MACE_TYPE).get())
                .ability(MKUAbilities.SEVER_TENDON, 3, 1.0)
                .ability(MKUAbilities.EMBER, 2, 0.5)
                .loot(LootSlotManager.ITEMS, lootTierName, 1.0)
                .lootDropChances(2)
                .noLootChance(.1)
                .noLootChanceIncrease(.25)
                .xp(10)
                .skillClass(NpcGenUtils.NpcSkillClass.FIGHTER)
                .build();
    }

    static NpcDefinition generateSkeletalTrooperMage() {
        return new NpcDefinitionBuilder(MKUltra.id("skeletal_trooper_mage"))
                .type(MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.0f)
                .renderGroup(MKUPiglins.DESTROYED_SKELETAL_MAGE_NAME)
                .health(50)
                .mana(50)
                .manaRegen(2.0)
                .name("Skeletal Magus")
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.DAGGER_TYPE).get())
                .ability(MKUAbilities.FIREBALL, 1, 1.0)
                .ability(MKUAbilities.EMBER, 2, 0.75)
                .ability(MKUAbilities.FLAME_WAVE, 3, 0.5)
                .ability(MKUAbilities.SMITE, 4, 0.25)
                .ability(MKUAbilities.FIRE_ARMOR, 5, 0.75)
                .xp(15)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateTrooperCaptain() {
        ResourceLocation lootTierName = MKUltra.id("trooper_captain");
        return new NpcDefinitionBuilder(MKUltra.id("trooper_captain"))
                .type(MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .renderGroup(MKUPiglins.SKELETAL_TROOPER_NAME)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.2f)
                .combo(10, 3)
                .health(100)
                .mana(100)
                .manaRegen(2)
                .titledFactionName("Captain", true)
                .lungeSpeed(1.0)
                .notable()
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.GREATSWORD_TYPE).get())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.KATANA_TYPE).get())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.WARHAMMER_TYPE).get())
                .ability(MKUAbilities.HEAL, 2, 1.0)
                .ability(MKUAbilities.FURIOUS_BROODING, 3, 1.0)
                .ability(MKUAbilities.SMITE, 1, 1.0)
                .loot(LootSlotManager.MAIN_HAND, lootTierName, 1.0)
                .loot(LootSlotManager.EARRINGS, lootTierName, 3.0)
                .lootDropChances(2)
                .noLootChance(0.2)
                .noLootChanceIncrease(0.25)
                .xp(25)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }

    static NpcDefinition generateImperialMagus() {
        ResourceLocation lootTierName = MKUltra.id("trooper_magus");
        return new NpcDefinitionBuilder(MKUltra.id("imperial_magus"))
                .type(MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.0f)
                .renderGroup(MKUPiglins.SKELETAL_MAGE_NAME)
                .health(150)
                .mana(150)
                .manaRegen(3.0)
                .titledFactionName("Imperial Magus", true)
                .notable()
                .lungeSpeed(0.5)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.MACE_TYPE).get())
                .ability(MKUAbilities.FIREBALL, 3, 1.0)
                .ability(MKUAbilities.FLAME_WAVE, 6, 1.0)
                .ability(MKUAbilities.SMITE, 4, 1.0)
                .ability(MKUAbilities.FIRE_ARMOR, 5, 1.0)
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .ability(MKUAbilities.POWER_WORD_SUMMON, 7, 1.0)
                .loot(LootSlotManager.MAIN_HAND, lootTierName, 1.0)
                .loot(LootSlotManager.RINGS, lootTierName, 2.0)
                .loot(LootSlotManager.EARRINGS, lootTierName, 1.0)
                .lootDropChances(2)
                .noLootChance(0.2)
                .noLootChanceIncrease(0.25)
                .xp(25)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateCrumblingTrooperMage() {
        return new NpcDefinitionBuilder(MKUltra.id("crumbling_trooper_mage"))
                .type(MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(0.95f)
                .renderGroup(MKUPiglins.ZOMBIE_PIG_MAGUS_NAME)
                .name("Crumbling Trooper Mage")
                .health(30.0)
                .mana(30.0)
                .manaRegen(1.0)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.DAGGER_TYPE).get())
                .ability(MKUAbilities.FIREBALL, 1, 1.0)
                .ability(MKUAbilities.EMBER, 2, 0.5)
                .loot(LootSlotManager.ITEMS, MKUltra.id("zombie_trooper"), 1.0)
                .lootDropChances(2)
                .noLootChance(0.1)
                .noLootChanceIncrease(0.25)
                .xp(10)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }


    static NpcDefinition generateDecayingZombieArcher() {
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

    static NpcDefinition generateTrooperExecution() {
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

    static NpcDefinition generateClericApprentice() {
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

    static NpcDefinition generateNetherMageInitiate() {
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

    static NpcDefinition generateBurningSkeleton() {
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

    static NpcDefinition generateDecayingZombiePiglin() {
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

    static NpcDefinition generateClericAcolyte() {
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

    static NpcDefinition generateForlornGhost() {
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
}
