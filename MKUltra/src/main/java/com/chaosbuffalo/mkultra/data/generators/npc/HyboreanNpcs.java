package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.chaosbuffalo.mkweapons.items.weapon.types.RangedWeaponTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;

public class HyboreanNpcs {

    public static final ResourceKey<NpcDefinition> hyborean_honor_guard = MKUNpcs.key("hyborean_honor_guard");
    public static final ResourceKey<NpcDefinition> hyborean_sorcerer = MKUNpcs.key("hyborean_sorcerer");
    public static final ResourceKey<NpcDefinition> an_ancient_king = MKUNpcs.key("an_ancient_king");
    public static final ResourceKey<NpcDefinition> hyborean_sorcerer_queen = MKUNpcs.key("hyborean_sorcerer_queen");
    public static final ResourceKey<NpcDefinition> hyborean_archer = MKUNpcs.key("hyborean_archer");
    public static final ResourceKey<NpcDefinition> hyborean_warrior = MKUNpcs.key("hyborean_warrior");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(hyborean_honor_guard, generateHyboreanHonorGuard(hyborean_honor_guard));
        context.register(hyborean_sorcerer, generateHyboreanSorcerer(hyborean_sorcerer));
        context.register(an_ancient_king, generateAncientKing(an_ancient_king));
        context.register(hyborean_sorcerer_queen, generateHyboreanSorcererQueen(hyborean_sorcerer_queen));
        context.register(hyborean_archer, generateHyboreanArcher(hyborean_archer));
        context.register(hyborean_warrior, generateHyboreanWarrior(hyborean_warrior));
    }


    static NpcDefinition generateHyboreanHonorGuard(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKFactions.UNDEAD)
                .name("Undying Honor Guard")
                .size(1.0f)
                .renderGroup(MKUSkeletons.HONOR_GUARD_NAME)
                .health(65)
                .mana(65)
                .manaRegen(1.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE), 10.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.GREATSWORD_TYPE), 10.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.WARHAMMER_TYPE), 10.0)
                .dropChance(1.1f)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.GREATSWORD_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.WARHAMMER_TYPE))
                .helmet(MKUItems.ancientBronzeHelmet)
                .boots(MKUItems.ancientBronzeBoots)
                .emptyChance(EquipmentSlot.FEET, 2.0)
                .emptyChance(EquipmentSlot.HEAD, 2.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .ability(MKUAbilities.SEVER_TENDON, 3, 1.0)
                .combo(30, 4)
                .xp(65)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }

    static NpcDefinition generateHyboreanSorcerer(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKFactions.UNDEAD)
                .name("Hyborean Sorcerer")
                .size(0.9f)
                .renderGroup(MKUSkeletons.SORCERER_NAME)
                .health(40)
                .mana(40)
                .manaRegen(2.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.DAGGER_TYPE), 5.0)
                .dropChance(1.1f)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.DAGGER_TYPE), 3.0)
                .ability(MKUAbilities.FIRE_ARMOR, 2, 0.5)
                .ability(MKUAbilities.FIREBALL, 1, 1.0)
                .ability(MKUAbilities.EMBER, 3, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .xp(65)
                .build();
    }

    static NpcDefinition generateAncientKing(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKFactions.UNDEAD)
                .name("An Ancient King")
                .size(1.15f)
                .renderGroup(MKUSkeletons.ANCIENT_KING_NAME)
                .health(165)
                .mana(165)
                .manaRegen(3.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.GREATSWORD_TYPE))
                .dropChance(1.1f)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.GREATSWORD_TYPE))
                .helmet(MKUItems.ancientBronzeHelmet)
                .boots(MKUItems.ancientBronzeBoots)
                .chestplate(MKUItems.ancientBronzeChestplate)
                .leggings(MKUItems.ancientBronzeLeggings)
                .emptyChance(EquipmentSlot.FEET, 2.0)
                .emptyChance(EquipmentSlot.HEAD, 2.0)
                .emptyChance(EquipmentSlot.CHEST, 2.0)
                .emptyChance(EquipmentSlot.LEGS, 2.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .ability(MKUAbilities.SEVER_TENDON, 3, 1.0)
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.POWER_WORD_SUMMON, 4, 0.5)
                .ability(MKUAbilities.EXPLOSIVE_GROWTH, 5, 0.5)
                .ability(MKUAbilities.FIREBALL, 6, 0.5)
                .loot(LootSlotManager.RINGS, MKULootTiers.ancient_king, 1.0)
                .loot(LootSlotManager.EARRINGS, MKULootTiers.ancient_king, 3.0)
                .lootDropChances(2)
                .noLootChance(0.2)
                .noLootChanceIncrease(0.25)
                .notable()
                .combo(10, 2)
                .xp(100)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }

    static NpcDefinition generateHyboreanSorcererQueen(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .name("Hyborean Sorcerer Queen")
                .faction(MKFactions.UNDEAD)
                .size(1.1f)
                .renderGroup(MKUSkeletons.SORCERER_QUEEN_NAME)
                .health(110)
                .mana(110)
                .manaRegen(3)
                .ability(MKUAbilities.FIRE_ARMOR, 5, 1.0)
                .ability(MKUAbilities.FIREBALL, 6, 1.0)
                .ability(MKUAbilities.EMBER, 1, 1.0)
                .ability(MKUAbilities.IGNITE, 2, 0.5)
                .ability(MKUAbilities.FLAME_WAVE, 3, 1.0)
                .ability(MKUAbilities.WARP_CURSE, 4, 0.5)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.KATANA_TYPE))
                .dropChance(1.1f)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.KATANA_TYPE))
                .loot(LootSlotManager.MAIN_HAND, MKULootTiers.hyborean_sorcerer_queen, 1.0)
                .loot(LootSlotManager.EARRINGS, MKULootTiers.hyborean_sorcerer_queen, 3.0)
                .lootDropChances(2)
                .noLootChance(0.2)
                .noLootChanceIncrease(0.25)
                .combo(60, 5)
                .xp(150)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .notable()
                .build();
    }

    static NpcDefinition generateHyboreanArcher(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .name("Decaying Archer")
                .faction(MKFactions.UNDEAD)
                .size(0.95f)
                .renderGroup(MKUSkeletons.HYBOREAN_ARCHER_NAME)
                .health(25)
                .mana(25)
                .manaRegen(1)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.STONE_TIER, RangedWeaponTypes.LONGBOW), 10.0)
                .dropChance(1.1f)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, RangedWeaponTypes.LONGBOW), 4.0)
                .xp(25)
                .skillClass(NpcGenUtils.NpcSkillClass.ARCHER)
                .build();
    }

    static NpcDefinition generateHyboreanWarrior(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKFactions.UNDEAD)
                .size(1.0f)
                .renderGroup(MKUSkeletons.HYBOREAN_WARRIOR_NAME)
                .name("Hyborean Warrior")
                .health(30)
                .mana(30)
                .manaRegen(1.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE), 10.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.SPEAR_TYPE), 10.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.GREATSWORD_TYPE), 10.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.LONGSWORD_TYPE), 10.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.STONE_TIER, MeleeWeaponTypes.WARHAMMER_TYPE), 10.0)
                .dropChance(1.1f)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.SPEAR_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.GREATSWORD_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.LONGSWORD_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.WARHAMMER_TYPE))
                .helmet(MKUItems.ancientBronzeHelmet)
                .boots(MKUItems.ancientBronzeBoots)
                .chestplate(MKUItems.ancientBronzeChestplate)
                .leggings(MKUItems.ancientBronzeLeggings)
                .emptyChance(EquipmentSlot.FEET, 20.0)
                .emptyChance(EquipmentSlot.HEAD, 20.0)
                .emptyChance(EquipmentSlot.CHEST, 20.0)
                .emptyChance(EquipmentSlot.LEGS, 20.0)
                .skillClass(NpcGenUtils.NpcSkillClass.WARRIOR)
                .xp(35)
                .build();
    }
}
