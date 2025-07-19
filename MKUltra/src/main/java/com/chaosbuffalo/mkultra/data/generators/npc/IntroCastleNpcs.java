package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUPiglins;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;


import java.util.UUID;

public class IntroCastleNpcs {

    public static final ResourceKey<NpcDefinition> crumbling_trooper = MKUNpcs.key("crumbling_trooper");
    public static final ResourceKey<NpcDefinition> skeletal_trooper_mage = MKUNpcs.key("skeletal_trooper_mage");
    public static final ResourceKey<NpcDefinition> trooper_captain = MKUNpcs.key("trooper_captain");
    public static final ResourceKey<NpcDefinition> imperial_magus = MKUNpcs.key("imperial_magus");
    public static final ResourceKey<NpcDefinition> crumbling_trooper_mage = MKUNpcs.key("crumbling_trooper_mage");
    public static final ResourceKey<NpcDefinition> decaying_piglin_archer = MKUNpcs.key("decaying_piglin_archer");
    public static final ResourceKey<NpcDefinition> trooper_executioner = MKUNpcs.key("trooper_executioner");
    public static final ResourceKey<NpcDefinition> solangian_apprentice = MKUNpcs.key("solangian_apprentice");
    public static final ResourceKey<NpcDefinition> nether_mage_initiate = MKUNpcs.key("nether_mage_initiate");
    public static final ResourceKey<NpcDefinition> burning_skeleton = MKUNpcs.key("burning_skeleton");
    public static final ResourceKey<NpcDefinition> decaying_piglin = MKUNpcs.key("decaying_piglin");
    public static final ResourceKey<NpcDefinition> solangian_acolyte = MKUNpcs.key("solangian_acolyte");
    public static final ResourceKey<NpcDefinition> forlorn_ghost = MKUNpcs.key("forlorn_ghost");

    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(crumbling_trooper, generateCrumblingTrooper(crumbling_trooper));
        context.register(skeletal_trooper_mage, generateSkeletalTrooperMage(skeletal_trooper_mage));
        context.register(trooper_captain, generateTrooperCaptain(trooper_captain));
        context.register(imperial_magus, generateImperialMagus(imperial_magus));
        context.register(crumbling_trooper_mage, generateCrumblingTrooperMage(crumbling_trooper_mage));
        context.register(decaying_piglin_archer, generateDecayingZombieArcher(decaying_piglin_archer));
        context.register(trooper_executioner, generateTrooperExecution(trooper_executioner));
        context.register(solangian_apprentice, generateClericApprentice(solangian_apprentice));
        context.register(nether_mage_initiate, generateNetherMageInitiate(nether_mage_initiate));
        context.register(burning_skeleton, generateBurningSkeleton(burning_skeleton));
        context.register(decaying_piglin, generateDecayingZombiePiglin(decaying_piglin));
        context.register(solangian_acolyte, generateClericAcolyte(solangian_acolyte));
        context.register(forlorn_ghost, generateForlornGhost(forlorn_ghost));
    }

    static NpcDefinition generateCrumblingTrooper(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.1f)
                .renderGroup(MKUPiglins.ZOMBIE_PIG_TROOPER_NAME)
                .name("Crumbling Trooper")
                .health(50)
                .mana(50)
                .manaRegen(1.0)
                .lungeSpeed(0.35)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.LONGSWORD_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.WARHAMMER_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.MACE_TYPE).orElseThrow())
                .ability(MKUAbilities.SEVER_TENDON, 3, 1.0)
                .ability(MKUAbilities.EMBER, 2, 0.5)
                .loot(LootSlotManager.ITEMS, MKULootTiers.zombie_trooper, 1.0)
                .lootDropChances(2)
                .noLootChance(.1)
                .noLootChanceIncrease(.25)
                .xp(10)
                .skillClass(NpcGenUtils.NpcSkillClass.FIGHTER)
                .build();
    }

    static NpcDefinition generateSkeletalTrooperMage(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.0f)
                .renderGroup(MKUPiglins.DESTROYED_SKELETAL_MAGE_NAME)
                .health(50)
                .mana(50)
                .manaRegen(2.0)
                .name("Skeletal Magus")
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.DAGGER_TYPE).orElseThrow())
                .ability(MKUAbilities.FIREBALL, 1, 1.0)
                .ability(MKUAbilities.EMBER, 2, 0.75)
                .ability(MKUAbilities.FLAME_WAVE, 3, 0.5)
                .ability(MKUAbilities.SMITE, 4, 0.25)
                .ability(MKUAbilities.FIRE_ARMOR, 5, 0.75)
                .xp(15)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateTrooperCaptain(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.GREATSWORD_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.KATANA_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.WARHAMMER_TYPE).orElseThrow())
                .ability(MKUAbilities.HEAL, 2, 1.0)
                .ability(MKUAbilities.FURIOUS_BROODING, 3, 1.0)
                .ability(MKUAbilities.SMITE, 1, 1.0)
                .loot(LootSlotManager.MAIN_HAND, MKULootTiers.trooper_captain, 1.0)
                .loot(LootSlotManager.EARRINGS, MKULootTiers.trooper_captain, 3.0)
                .lootDropChances(2)
                .noLootChance(0.2)
                .noLootChanceIncrease(0.25)
                .xp(25)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }

    static NpcDefinition generateImperialMagus(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.0f)
                .renderGroup(MKUPiglins.SKELETAL_MAGE_NAME)
                .health(150)
                .mana(150)
                .manaRegen(3.0)
                .titledFactionName("Imperial Magus", true)
                .notable()
                .lungeSpeed(0.5)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.MACE_TYPE).orElseThrow())
                .ability(MKUAbilities.FIREBALL, 3, 1.0)
                .ability(MKUAbilities.FLAME_WAVE, 6, 1.0)
                .ability(MKUAbilities.SMITE, 4, 1.0)
                .ability(MKUAbilities.FIRE_ARMOR, 5, 1.0)
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .ability(MKUAbilities.POWER_WORD_SUMMON, 7, 1.0)
                .loot(LootSlotManager.MAIN_HAND, MKULootTiers.trooper_magus, 1.0)
                .loot(LootSlotManager.RINGS, MKULootTiers.trooper_magus, 2.0)
                .loot(LootSlotManager.EARRINGS, MKULootTiers.trooper_magus, 1.0)
                .lootDropChances(2)
                .noLootChance(0.2)
                .noLootChanceIncrease(0.25)
                .xp(25)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateCrumblingTrooperMage(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(0.95f)
                .renderGroup(MKUPiglins.ZOMBIE_PIG_MAGUS_NAME)
                .name("Crumbling Trooper Mage")
                .health(30.0)
                .mana(30.0)
                .manaRegen(1.0)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.DAGGER_TYPE).orElseThrow())
                .ability(MKUAbilities.FIREBALL, 1, 1.0)
                .ability(MKUAbilities.EMBER, 2, 0.5)
                .loot(LootSlotManager.ITEMS, MKULootTiers.zombie_trooper, 1.0)
                .lootDropChances(2)
                .noLootChance(0.1)
                .noLootChanceIncrease(0.25)
                .xp(10)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }


    static NpcDefinition generateDecayingZombieArcher(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(0.85f)
                .renderGroup(MKUPiglins.ZOMBIE_PIG_NAME)
                .health(15.0)
                .mana(15.0)
                .manaRegen(1.0)
                .name("Shambling Archer")
                .mainHand(Holder.direct(Items.BOW))
                .xp(5)
                .skillClass(NpcGenUtils.NpcSkillClass.ARCHER)
                .build();
    }

    static NpcDefinition generateTrooperExecution(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(1.0f)
                .renderGroup(MKUPiglins.DESTROYED_SKELETAL_TROOPER_NAME)
                .combo(20, 3)
                .health(180.0)
                .mana(180.0)
                .manaRegen(2.0)
                .name("Trooper Executioner")
                .lungeSpeed(0.75)
                .notable()
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE).orElseThrow())
                .ability(MKUAbilities.FURIOUS_BROODING, 3, 1.0)
                .ability(MKUAbilities.WHIRLWIND_BLADES, 1, 1.0)
                .ability(MKUAbilities.YANK, 2, 1.0)
                .loot(LootSlotManager.MAIN_HAND, MKULootTiers.trooper_executioner, 1.0)
                .loot(LootSlotManager.RINGS, MKULootTiers.trooper_executioner, 3.0)
                .lootDropChances(2)
                .noLootChance(0.2)
                .noLootChanceIncrease(0.25)
                .xp(20)
                .skillClass(NpcGenUtils.NpcSkillClass.FIGHTER)
                .build();
    }


    static NpcDefinition generateClericApprentice(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.SEE_OF_SOLANG_NAME)
                .size(0.85f)
                .renderGroup(MKUHumans.CLERIC_2_NAME)
                .health(100.0)
                .mana(100.0)
                .manaRegen(2.5)
                .titledFactionName("Apprentice", false)
                .notable()
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.MACE_TYPE).orElseThrow())
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.SMITE, 2, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.CLERIC)
                .build();
    }

    static NpcDefinition generateNetherMageInitiate(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.NETHER_MAGE_NAME)
                .size(0.90f)
                .renderGroup(MKUHumans.NETHER_MAGE_1_NAME)
                .health(150.0)
                .mana(150.0)
                .manaRegen(3.0)
                .titledFactionName("Initiate", false)
                .notable()
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.WOOD_TIER, MeleeWeaponTypes.STAFF_TYPE).orElseThrow())
                .ability(MKUAbilities.EMBER, 1, 1.0)
                .ability(MKUAbilities.FIREBALL, 2, 1.0)
                .ability(MKUAbilities.FLAME_WAVE, 3, 1.0)
                .trains(MKUAbilities.EMBER, new HasEntitlementRequirement(MKUEntitlements.IntroNetherMageTier1.get()))
                .trains(MKUAbilities.FIRE_ARMOR, new HasEntitlementRequirement(MKUEntitlements.IntroNetherMageTier1.get()))
                .dialogue(MKUltra.id("intro_nether_mage_initiate"))
                .quests(MKUQuests.NETHER_MAGE_INTRO)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateBurningSkeleton(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.HYBOREAN_DEAD_NAME)
                .size(1.0f)
                .renderGroup(MKUSkeletons.BURNING_NAME)
                .attribute(Attributes.MAX_HEALTH, 250.0)
                .attribute(Attributes.ARMOR, 10.0)
                .attribute(Attributes.ATTACK_DAMAGE, 5.0)
                .attribute(MKAttributes.BLEED_RESISTANCE, 1.25)
                .attribute(MKAttributes.MAX_MANA, 250.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("Burning Revenant")
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.DAGGER_TYPE).orElseThrow())
                .notable()
                .bossStage(new BossStage()
                        .withOption(new TempAbilitiesOption()
                                .withAbilityOption(MKUAbilities.FIRE_ARMOR.get(), 3, 1.0)
                                .withAbilityOption(MKUAbilities.FIREBALL.get(), 2, 1.0)
                                .withAbilityOption(MKUAbilities.WRATH_BEAM.get(), 1, 1.0)))
                .bossStage(new BossStage()
                        .withOption(new TempAbilitiesOption()
                                .withAbilityOption(MKUAbilities.FIREBALL.get(), 3, 1.0)
                                .withAbilityOption(MKUAbilities.WRATH_BEAM_FLURRY.get(), 1, 1.0))
                        .withParticleMode(BossStage.ParticleMode.LINE_HEIGHT)
                        .withTransitionParticles(MKUltra.id("wrath_skeleton_transition"))
                        .withTransitionSound(MKUSounds.spell_dark_8.getId()))
                .xp(50)
                .particles(
                        new BoneEffectInstance(UUID.fromString("3e7496f1-f5bf-45e6-b8e5-64192633ae9f"),
                                MKUltra.id("burning_skeleton_head"), BipedSkeleton.HEAD_BONE_NAME)
                )
                .loot(LootSlotManager.MAIN_HAND, MKULootTiers.burning_skeleton, 1.0)
                .loot(LootSlotManager.RINGS, MKULootTiers.burning_skeleton, 3.0)
                .loot(LootSlotManager.EARRINGS, MKULootTiers.burning_skeleton, 2.0)
                .dropChance(1)
                .noLootChance(0.1)
                .noLootChanceIncrease(0.0)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateDecayingZombiePiglin(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
                .faction(MKUFactions.IMPERIAL_DEAD_NAME)
                .size(0.9f)
                .renderGroup(MKUPiglins.ZOMBIE_PIG_NAME)
                .health(20.0)
                .mana(20.0)
                .manaRegen(1.0)
                .name("Decaying Zombie")
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.DAGGER_TYPE).orElseThrow())
                .emptyChance(EquipmentSlot.MAINHAND, 1.0)
                .xp(5)
                .skillClass(NpcGenUtils.NpcSkillClass.FIGHTER)
                .build();
    }

    static NpcDefinition generateClericAcolyte(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.SEE_OF_SOLANG_NAME)
                .size(1.05f)
                .renderGroup(MKUHumans.CLERIC_1_NAME)
                .health(150.0)
                .mana(150.0)
                .manaRegen(3.5)
                .titledFactionName("Acolyte", false)
                .notable()
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.MACE_TYPE).orElseThrow())
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.SMITE, 2, 1.0)
                .ability(MKUAbilities.GALVANIZE, 3, 1.0)
                .dialogue(MKUltra.id("intro_cleric_acolyte"))
                .quests(MKUQuests.CLERIC_INTRO)
                .trains(MKUAbilities.HEAL, new HasEntitlementRequirement(MKUEntitlements.IntroClericTier1.get()))
                .trains(MKUAbilities.SMITE, new HasEntitlementRequirement(MKUEntitlements.IntroClericTier1.get()))
                .skillClass(NpcGenUtils.NpcSkillClass.CLERIC)
                .build();
    }

    static NpcDefinition generateForlornGhost(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.SEE_OF_SOLANG_NAME)
                .size(1.0f)
                .renderGroup(MKUHumans.GHOST_1_NAME)
                .health(100.0)
                .mana(100.0)
                .manaRegen(3.0)
                .name("Forlorn Ghost")
                .notable()
                .ghost(0.7f)
                .skillClass(NpcGenUtils.NpcSkillClass.FIGHTER)
                .build();
    }
}
