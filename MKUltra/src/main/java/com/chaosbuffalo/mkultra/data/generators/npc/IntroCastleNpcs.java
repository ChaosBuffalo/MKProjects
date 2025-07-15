package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
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
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IntroCastleNpcs {

    public static CompletableFuture<?> writeDefinitions(NpcDefinitionProvider provider, CachedOutput cache) {
        return CompletableFuture.allOf(
                provider.writeDefinition(generateCrumblingTrooper(), cache),
                provider.writeDefinition(generateCrumblingTrooperMage(), cache),
                provider.writeDefinition(generateDecayingZombieArcher(), cache),
                provider.writeDefinition(generateDecayingZombiePiglin(), cache),
                provider.writeDefinition(generateImperialMagus(), cache),
                provider.writeDefinition(generateTrooperCaptain(), cache),
                provider.writeDefinition(generateTrooperExecution(), cache),
                provider.writeDefinition(generateSkeletalTrooperMage(), cache),
                provider.writeDefinition(generateBurningSkeleton(), cache),
                provider.writeDefinition(generateClericAcolyte(), cache),
                provider.writeDefinition(generateClericApprentice(), cache),
                provider.writeDefinition(generateForlornGhost(), cache),
                provider.writeDefinition(generateNetherMageInitiate(), cache)
        );
    }

    static NpcDefinition generateCrumblingTrooper() {
        return new NpcDefinitionBuilder(MKUltra.id("crumbling_trooper"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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

    static NpcDefinition generateSkeletalTrooperMage() {
        return new NpcDefinitionBuilder(MKUltra.id("skeletal_trooper_mage"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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

    static NpcDefinition generateTrooperCaptain() {
        return new NpcDefinitionBuilder(MKUltra.id("trooper_captain"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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

    static NpcDefinition generateImperialMagus() {
        return new NpcDefinitionBuilder(MKUltra.id("imperial_magus"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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

    static NpcDefinition generateCrumblingTrooperMage() {
        return new NpcDefinitionBuilder(MKUltra.id("crumbling_trooper_mage"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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


    static NpcDefinition generateDecayingZombieArcher() {
        return new NpcDefinitionBuilder(MKUltra.id("decaying_piglin_archer"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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

    static NpcDefinition generateTrooperExecution() {
        return new NpcDefinitionBuilder(MKUltra.id("trooper_executioner"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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


    static NpcDefinition generateClericApprentice() {
        return new NpcDefinitionBuilder(MKUltra.id("solangian_apprentice"), MKUEntities.HUMAN_TYPE)
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

    static NpcDefinition generateNetherMageInitiate() {
        return new NpcDefinitionBuilder(MKUltra.id("nether_mage_initiate"), MKUEntities.HUMAN_TYPE)
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
                .quests(MKUltra.id("nether_mage_intro"))
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateBurningSkeleton() {
        return new NpcDefinitionBuilder(MKUltra.id("burning_skeleton"), MKUEntities.HYBOREAN_SKELETON_TYPE)
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

    static NpcDefinition generateDecayingZombiePiglin() {
        return new NpcDefinitionBuilder(MKUltra.id("decaying_piglin"), MKUEntities.ZOMBIFIED_PIGLIN_TYPE)
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

    static NpcDefinition generateClericAcolyte() {
        return new NpcDefinitionBuilder(MKUltra.id("solangian_acolyte"), MKUEntities.HUMAN_TYPE)
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
                .quests(MKUltra.id("cleric_intro"))
                .trains(MKUAbilities.HEAL, new HasEntitlementRequirement(MKUEntitlements.IntroClericTier1.get()))
                .trains(MKUAbilities.SMITE, new HasEntitlementRequirement(MKUEntitlements.IntroClericTier1.get()))
                .skillClass(NpcGenUtils.NpcSkillClass.CLERIC)
                .build();
    }

    static NpcDefinition generateForlornGhost() {
        return new NpcDefinitionBuilder(MKUltra.id("forlorn_ghost"), MKUEntities.HUMAN_TYPE)
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
