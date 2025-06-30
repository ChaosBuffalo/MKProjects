package com.chaosbuffalo.mkultra.data.generators.npc;


import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUGolems;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.concurrent.CompletableFuture;

public class NecrotideNpcs {

    public static CompletableFuture<?> writeDefinitions(NpcDefinitionProvider provider, CachedOutput cache) {
        return CompletableFuture.allOf(
                provider.writeDefinition(generateNecrotideCultistAcolyte(), cache),
                provider.writeDefinition(generateNecrotideCultist(), cache),
                provider.writeDefinition(generateSkeletalLock(), cache),
                provider.writeDefinition(generateNecrotideGolem(), cache),
                provider.writeDefinition(generateNecrotideSkeletalArcher(), cache),
                provider.writeDefinition(generateNecrotideSkeletalWarrior(), cache)
        );
    }

    static NpcDefinition generateNecrotideCultist() {

        return new NpcDefinitionBuilder(MKUltra.id("necrotide_cultist"))
                .type(MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUHumans.NECROTIDE_CULTIST_SKULL_1_NAME)
                .size(1.05f)
                .attribute(Attributes.MAX_HEALTH, 125.0)
                .attribute(MKAttributes.MAX_MANA, 125.0)
                .attribute(MKAttributes.MANA_REGEN, 3.0)
                .titledFactionName("Cultist")
                .ability(MKUAbilities.SHADOW_BOLT, 1, 1.0)
                .ability(MKUAbilities.DROWN, 2, 1.0)
                .ability(MKUAbilities.SHADOW_PULSE, 3, 1.0)
                .ability(MKUAbilities.NECROTIDE_WARRIOR_SUMMON, 4, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .xp(65)
                .battlecry()
                .build();
    }

    static NpcDefinition generateSkeletalLock() {
        return new NpcDefinitionBuilder(MKUltra.id("skeletal_lock"))
                .type(MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUSkeletons.BASIC_NAME)
                .size(1.0f)
                .attribute(Attributes.MAX_HEALTH, 50.0)
                .attribute(MKAttributes.MAX_MANA, 50.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("A Skeletal Lock")
                .ability(MKUAbilities.SHADOW_BOLT, 1, 1.0)
                .ability(MKUAbilities.SHADOW_PULSE, 2, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .xp(45)
                .notable()
                .build();
    }

    static NpcDefinition generateNecrotideGolem() {
        ResourceLocation lootTierName = MKUltra.id("necrotide_golem");
        return new NpcDefinitionBuilder(MKUltra.id("necrotide_golem"))
                .type(MKUEntities.GOLEM_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUGolems.NECROTIDE_GOLEM_NAME)
                .size(1.25f)
                .attribute(Attributes.MAX_HEALTH, 500.0)
                .attribute(MKAttributes.MAX_MANA, 500.0)
                .attribute(MKAttributes.MANA_REGEN, 5.0)
                .name("A Necrotide Construction")
                .ability(MKUAbilities.SHADOW_BOLT, 2, 1.0)
                .ability(MKUAbilities.NECROTIDE_GOLEM_BEAM, 1, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .notable()
                .loot(LootSlotManager.RINGS, lootTierName, 3.0)
                .loot(LootSlotManager.HANDS, lootTierName, 1.0)
                .lootDropChances(1)
                .noLootChance(0.0)
                .noLootChanceIncrease(0.0)
                .xp(500)
                .build();
    }

    static NpcDefinition generateNecrotideCultistAcolyte() {
        return new NpcDefinitionBuilder(MKUltra.id("necrotide_acolyte"))
                .type(MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUHumans.NECROTIDE_CULTIST_1_NAME)
                .size(0.95f)
                .attribute(Attributes.MAX_HEALTH, 50.0)
                .attribute(MKAttributes.MAX_MANA, 50.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("A Necrotide Acolyte")
                .ability(MKUAbilities.SHADOW_BOLT, 1, 1.0)
                .ability(MKUAbilities.DROWN, 2, 0.5)
                .ability(MKUAbilities.SHADOW_PULSE, 3, 0.5)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .xp(30)
                .battlecry()
                .build();
    }

    static NpcDefinition generateNecrotideSkeletalWarrior() {
        return new NpcDefinitionBuilder(MKUltra.id("necrotide_skeletal_warrior"))
                .type(MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUSkeletons.BASIC_NAME)
                .size(1.0f)
                .attribute(Attributes.MAX_HEALTH, 65.0)
                .attribute(MKAttributes.MAX_MANA, 65.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("Skeleton Warrior")
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.SPEAR_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.GREATSWORD_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.LONGSWORD_TYPE).orElseThrow())
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.WARHAMMER_TYPE).orElseThrow())
                .skillClass(NpcGenUtils.NpcSkillClass.WARRIOR)
                .xp(30)
                .build();
    }

    static NpcDefinition generateNecrotideSkeletalArcher() {
        return new NpcDefinitionBuilder(MKUltra.id("necrotide_skeletal_archer"))
                .type(MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUSkeletons.HYBOREAN_ARCHER_NAME)
                .size(0.95f)
                .attribute(Attributes.MAX_HEALTH, 30.0)
                .attribute(MKAttributes.MAX_MANA, 30.0)
                .attribute(MKAttributes.MANA_REGEN, 1.0)
                .name("Skeleton Archer")
                .mainHand(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("mkweapons:longbow_iron")).orElseThrow())
                .skillClass(NpcGenUtils.NpcSkillClass.ARCHER)
                .xp(25)
                .build();
    }
}
