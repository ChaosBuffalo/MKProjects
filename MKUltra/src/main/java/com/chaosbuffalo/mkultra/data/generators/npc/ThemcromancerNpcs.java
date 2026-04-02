package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ThemcromancerNpcs {

    public static final ResourceKey<NpcDefinition> themcromancer_acolyte = MKUNpcs.key("themcromancer_acolyte");
    public static final ResourceKey<NpcDefinition> themcromancer_neophyte = MKUNpcs.key("themcromancer_neophyte");
    public static final ResourceKey<NpcDefinition> themcromancer_archon = MKUNpcs.key("themcromancer_archon");
    public static final ResourceKey<NpcDefinition> themcromancer_librarian = MKUNpcs.key("themcromancer_librarian");
    public static final ResourceKey<NpcDefinition> a_skeletal_gatekeeper = MKUNpcs.key("a_skeletal_gatekeeper");
    public static final ResourceKey<NpcDefinition> a_skeletal_guard = MKUNpcs.key("a_skeletal_guard");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(themcromancer_acolyte, generateThemnianAcolyte(themcromancer_acolyte));
        context.register(themcromancer_neophyte, generateThemnianNeophyte(themcromancer_neophyte));
        context.register(themcromancer_archon, generateThemnianArchon(themcromancer_archon, context));
        context.register(themcromancer_librarian, generateThemnianLibrarian(themcromancer_librarian));
        context.register(a_skeletal_gatekeeper, generateSkeletalGatekeeper(a_skeletal_gatekeeper));
        context.register(a_skeletal_guard, generateSkeletalGuard(a_skeletal_guard));
    }


    static NpcDefinition generateThemnianAcolyte(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.THEMCROMANCERS_NAME)
                .renderGroup(MKUHumans.DEFAULT_NAME)
                .size(0.92f)
                .attribute(Attributes.MAX_HEALTH, 90.0)
                .attribute(MKAttributes.MAX_MANA, 90.0)
                .attribute(MKAttributes.MANA_REGEN, 3.0)
                .name("Themnian Acolyte")
                .ability(MKUAbilities.SHADOW_BOLT, 1, 1.0)
                .ability(MKUAbilities.SHADOW_PULSE, 2, 1.0)
                .ability(MKUAbilities.FIREBALL, 3, 0.5)
                .ability(MKUAbilities.NECROTIDE_WARRIOR_SUMMON, 3, 1.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.DAGGER_TYPE))
                .dropChance(0.05f)
                .helmet(MKUItems.themnianHelmet)
                .boots(MKUItems.themnianBoots)
                .chestplate(MKUItems.themnianChestplate)
                .leggings(MKUItems.themnianLeggings)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .xp(75)
                .build();
    }

    static NpcDefinition generateThemnianNeophyte(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.THEMCROMANCERS_NAME)
                .renderGroup(MKUHumans.DEFAULT_NAME)
                .size(0.85f)
                .attribute(Attributes.MAX_HEALTH, 40.0)
                .attribute(MKAttributes.MAX_MANA, 40.0)
                .attribute(MKAttributes.MANA_REGEN, 1.0)
                .name("Themnian Neophyte")
                .ability(MKUAbilities.SHADOW_BOLT, 1, 1.0)
                .ability(MKUAbilities.ENGULFING_DARKNESS, 2, 0.5)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.DAGGER_TYPE))
                .dropChance(0.05f)
                .helmet(MKUItems.themnianHelmet)
                .boots(MKUItems.themnianBoots)
                .chestplate(MKUItems.themnianChestplate)
                .leggings(MKUItems.themnianLeggings)
                .xp(50)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .build();
    }

    static NpcDefinition generateThemnianArchon(ResourceKey<NpcDefinition> key, BootstrapContext<NpcDefinition> context) {
        var entitlements = context.lookup(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);

        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.THEMCROMANCERS_NAME)
                .renderGroup(MKUHumans.DEFAULT_NAME)
                .size(1.0f)
                .attribute(Attributes.MAX_HEALTH, 300.0)
                .attribute(MKAttributes.MAX_MANA, 300.0)
                .attribute(MKAttributes.MANA_REGEN, 3.0)
                .titledFactionName("Archon")
                .ability(MKUAbilities.SHADOW_BOLT_DUAL_SHOTGUN, 1, 1.0)
                .ability(MKUAbilities.ENGULFING_DARKNESS, 2, 1.0)
                .ability(MKUAbilities.LIFE_SPIKE, 3, 1.0)
                .ability(MKUAbilities.WRATH_BEAM, 4, 1.0)
                .ability(MKUAbilities.SHADOW_PUlSE_FLURRY, 5, 1.0)
                .dropChance(0.05f)
                .notable()
                .loot(LootSlotManager.RINGS, MKULootTiers.themcromancer_archon, 3.0)
                .loot(LootSlotManager.EARRINGS, MKULootTiers.themcromancer_archon, 2.0)
                .dropChance(1)
                .noLootChance(0.25)
                .noLootChanceIncrease(0.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.STAFF_TYPE))
                .helmet(MKUItems.themnianLeaderHelmet)
                .boots(MKUItems.themnianLeaderBoots)
                .chestplate(MKUItems.themnianLeaderChestplate)
                .leggings(MKUItems.themnianLeaderLeggings)
                .trains(MKUAbilities.NECROTIDE_WARRIOR_SUMMON, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ThemcromancerTier1)))
                .trains(MKUAbilities.ENGULFING_DARKNESS, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ThemcromancerTier1)))
                .trains(MKUAbilities.SHADOW_BOLT, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ThemcromancerTier2)))
                .trains(MKUAbilities.SHADOW_PULSE, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ThemcromancerTier2)))
                .trains(MKUAbilities.LIFE_SPIKE, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ThemcromancerTier3)))
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .dialogue(MKUDialogues.necro_default)
                .xp(150)
                .quests(MKUQuests.NECROMANCER_UNLOCK_CHAIN)
                .build();
    }

    static NpcDefinition generateThemnianLibrarian(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.THEMCROMANCERS_NAME)
                .renderGroup(MKUHumans.DEFAULT_NAME)
                .size(0.92f)
                .attribute(Attributes.MAX_HEALTH, 250.0)
                .attribute(MKAttributes.MAX_MANA, 250.0)
                .attribute(MKAttributes.MANA_REGEN, 5.0)
                .titledFactionName("Librarian")
                .notable()
                .loot(LootSlotManager.MAIN_HAND, MKULootTiers.themcromancer_librarian, 3.0)
                .dropChance(1)
                .noLootChance(0.25)
                .noLootChanceIncrease(0.0)
                .ability(MKUAbilities.SHADOW_BOLT_DUAL_SHOTGUN, 1, 1.0)
                .ability(MKUAbilities.SHADOW_PUlSE_FLURRY, 2, 1.0)
                .ability(MKUAbilities.FIREBALL_BURST, 3, 1.0)
                .ability(MKUAbilities.NECROTIDE_WARRIOR_SUMMON, 3, 1.0)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.DAGGER_TYPE))
                .dropChance(0.05f)
                .helmet(MKUItems.themnianHelmet)
                .boots(MKUItems.themnianBoots)
                .chestplate(MKUItems.themnianChestplate)
                .leggings(MKUItems.themnianLeggings)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .xp(100)
                .build();
    }

    static NpcDefinition generateSkeletalGatekeeper(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.THEMCROMANCER_GATEKEEPER_NAME)
                .renderGroup(MKUSkeletons.BASIC_NAME)
                .size(1.0f)
                .attribute(Attributes.MAX_HEALTH, 600.0)
                .attribute(MKAttributes.MAX_MANA, 600.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("a skeletal gatekeeper")
                .notable()
                .quests(MKUQuests.UNLOCK_THEMCROMANCERS)
                .skillClass(NpcGenUtils.NpcSkillClass.WARRIOR)
                .xp(100)
                .build();
    }

    static NpcDefinition generateSkeletalGuard(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.THEMCROMANCERS_NAME)
                .renderGroup(MKUSkeletons.BASIC_NAME)
                .size(1.1f)
                .attribute(Attributes.MAX_HEALTH, 100.0)
                .attribute(MKAttributes.MAX_MANA, 100.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("a skeletal guard")
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.SPEAR_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.GREATSWORD_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.LONGSWORD_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.WARHAMMER_TYPE))
                .dropChance(0.05f)
                .chestplate(MKUItems.ancientBronzeChestplate)
                .boots(MKUItems.ancientBronzeBoots)
                .ability(MKUAbilities.SEVER_TENDON, 1, 1.0)
                .ability(MKUAbilities.FURIOUS_BROODING, 2, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.WARRIOR)
                .xp(50)
                .build();
    }
}
