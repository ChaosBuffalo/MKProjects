package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.concurrent.CompletableFuture;

public class ThemcromancerNpcs {

    public static CompletableFuture<?> writeDefinitions(NpcDefinitionProvider provider, CachedOutput cache) {
        return CompletableFuture.allOf(
                provider.writeDefinition(generateThemnianAcolyte(), cache),
                provider.writeDefinition(generateThemnianNeophyte(), cache),
                provider.writeDefinition(generateThemnianArchon(), cache),
                provider.writeDefinition(generateSkeletalGatekeeper(), cache),
                provider.writeDefinition(generateSkeletalGuard(), cache),
                provider.writeDefinition(generateThemnianLibrarian(), cache)
        );
    }

    static NpcDefinition generateThemnianAcolyte() {

        return new NpcDefinitionBuilder(MKUltra.id("themcromancer_acolyte"), MKUEntities.HUMAN_TYPE)
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
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.DAGGER_TYPE).orElseThrow())
                .dropChance(0.05f)
                .helmet(MKUItems.themnianHelmet)
                .boots(MKUItems.themnianBoots)
                .chestplate(MKUItems.themnianChestplate)
                .leggings(MKUItems.themnianLeggings)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .xp(75)
                .build();
    }

    static NpcDefinition generateThemnianNeophyte() {

        return new NpcDefinitionBuilder(MKUltra.id("themcromancer_neophyte"), MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.THEMCROMANCERS_NAME)
                .renderGroup(MKUHumans.DEFAULT_NAME)
                .size(0.85f)
                .attribute(Attributes.MAX_HEALTH, 40.0)
                .attribute(MKAttributes.MAX_MANA, 40.0)
                .attribute(MKAttributes.MANA_REGEN, 1.0)
                .name("Themnian Neophyte")
                .ability(MKUAbilities.SHADOW_BOLT, 1, 1.0)
                .ability(MKUAbilities.ENGULFING_DARKNESS, 2, 0.5)
                .mainHand(MKWeaponsItems.lookupMelee(MKUItems.BRONZE_TIER, MeleeWeaponTypes.DAGGER_TYPE, MKUltra.MODID).orElseThrow())
                .dropChance(0.05f)
                .helmet(MKUItems.themnianHelmet)
                .boots(MKUItems.themnianBoots)
                .chestplate(MKUItems.themnianChestplate)
                .leggings(MKUItems.themnianLeggings)
                .xp(50)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .build();
    }

    static NpcDefinition generateThemnianArchon() {

        return new NpcDefinitionBuilder(MKUltra.id("themcromancer_archon"), MKUEntities.HUMAN_TYPE)
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
                .mainHand(MKWeaponsItems.lookupMelee(MKUItems.BRONZE_TIER, MeleeWeaponTypes.STAFF_TYPE, MKUltra.MODID).orElseThrow())
                .helmet(MKUItems.themnianLeaderHelmet)
                .boots(MKUItems.themnianLeaderBoots)
                .chestplate(MKUItems.themnianLeaderChestplate)
                .leggings(MKUItems.themnianLeaderLeggings)
                .trains(MKUAbilities.NECROTIDE_WARRIOR_SUMMON, new HasEntitlementRequirement(MKUEntitlements.ThemcromancerTier1.get()))
                .trains(MKUAbilities.ENGULFING_DARKNESS, new HasEntitlementRequirement(MKUEntitlements.ThemcromancerTier1.get()))
                .trains(MKUAbilities.SHADOW_BOLT, new HasEntitlementRequirement(MKUEntitlements.ThemcromancerTier2.get()))
                .trains(MKUAbilities.SHADOW_PULSE, new HasEntitlementRequirement(MKUEntitlements.ThemcromancerTier2.get()))
                .trains(MKUAbilities.LIFE_SPIKE, new HasEntitlementRequirement(MKUEntitlements.ThemcromancerTier3.get()))
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .dialogue(MKUltra.id("necro_default"))
                .xp(150)
                .quests(MKUQuests.NECROMANCER_UNLOCK_CHAIN)
                .build();
    }

    static NpcDefinition generateThemnianLibrarian() {

        return new NpcDefinitionBuilder(MKUltra.id("themcromancer_librarian"), MKUEntities.HUMAN_TYPE)
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
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.DAGGER_TYPE).orElseThrow())
                .dropChance(0.05f)
                .helmet(MKUItems.themnianHelmet)
                .boots(MKUItems.themnianBoots)
                .chestplate(MKUItems.themnianChestplate)
                .leggings(MKUItems.themnianLeggings)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .xp(100)
                .build();
    }

    static NpcDefinition generateSkeletalGatekeeper() {
        return new NpcDefinitionBuilder(MKUltra.id("a_skeletal_gatekeeper"), MKUEntities.HYBOREAN_SKELETON_TYPE)
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

    static NpcDefinition generateSkeletalGuard() {
        return new NpcDefinitionBuilder(MKUltra.id("a_skeletal_guard"), MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.THEMCROMANCERS_NAME)
                .renderGroup(MKUSkeletons.BASIC_NAME)
                .size(1.1f)
                .attribute(Attributes.MAX_HEALTH, 100.0)
                .attribute(MKAttributes.MAX_MANA, 100.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("a skeletal guard")
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.SPEAR_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.GREATSWORD_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.LONGSWORD_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.WARHAMMER_TYPE).orElseThrow())
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
