package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.CachedOutput;

import java.util.concurrent.CompletableFuture;

public class ClericNpcs {

    public static CompletableFuture<?> writeDefinitions(NpcDefinitionProvider provider, CachedOutput cache) {
        return CompletableFuture.allOf(
                provider.writeDefinition(generateTempleGuard(), cache),
                provider.writeDefinition(generateTempleGuard2(), cache),
                provider.writeDefinition(generateCleric(), cache)
        );
    }


    static NpcDefinition generateCleric() {
        return new NpcDefinitionBuilder(MKUltra.id("solangian_cleric"), MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.SEE_OF_SOLANG_NAME)
                .size(1.05f)
                .renderGroup(MKUHumans.CLERIC_1_NAME)
                .titledFactionName("Cleric")
                .notable()
                .health(500.0)
                .mana(500.0)
                .manaRegen(5.0)
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.SMITE, 2, 1.0)
                .ability(MKUAbilities.GALVANIZE, 3, 1.0)
                .ability(MKUAbilities.POWER_WORD_SUMMON, 4, 1.0)
                .ability(MKUAbilities.INSPIRE, 5, 1.0)
                .dialogue(MKUltra.id("cleric_default"))
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.MACE_TYPE).orElseThrow(), 1.0)
                .quests(MKUQuests.CLERIC_UNLOCK_CHAIN)
                .trains(MKUAbilities.HEAL, new HasEntitlementRequirement(MKUEntitlements.ClericTier1.get()))
                .trains(MKUAbilities.SMITE, new HasEntitlementRequirement(MKUEntitlements.ClericTier1.get()))
                .trains(MKUAbilities.GALVANIZE, new HasEntitlementRequirement(MKUEntitlements.ClericTier2.get()))
                .trains(MKUAbilities.POWER_WORD_SUMMON, new HasEntitlementRequirement(MKUEntitlements.ClericTier2.get()))
                .trains(MKUAbilities.INSPIRE, new HasEntitlementRequirement(MKUEntitlements.ClericTier3.get()))
                .skillClass(NpcGenUtils.NpcSkillClass.CLERIC)
                .xp(100)
                .build();
    }

    static NpcDefinition generateTempleGuard2() {
        return new NpcDefinitionBuilder(MKUltra.id("solangian_temple_guard_2"), MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.SEE_OF_SOLANG_NAME)
                .renderGroup(MKUHumans.TEMPLE_GUARD_2_NAME)
                .size(1.0f)
                .health(300.0)
                .mana(300.0)
                .manaRegen(4.0)
                .titledFactionName("Temple Guard")
                .notable()
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.SMITE, 2, 1.0)
                .ability(MKUAbilities.SEVER_TENDON, 3, 1.0)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.SPEAR_TYPE).orElseThrow(), 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .xp(65)
                .build();
    }

    static NpcDefinition generateTempleGuard() {
        return new NpcDefinitionBuilder(MKUltra.id("solangian_temple_guard"), MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.SEE_OF_SOLANG_NAME)
                .renderGroup(MKUHumans.TEMPLE_GUARD_1_NAME)
                .size(1.0f)
                .health(250.0)
                .mana(250.0)
                .manaRegen(3.0)
                .titledFactionName("Temple Guard")
                .notable()
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.SMITE, 2, 1.0)
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.SPEAR_TYPE).orElseThrow(), 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .xp(50)
                .build();
    }
}
