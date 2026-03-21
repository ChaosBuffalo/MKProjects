package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class ClericNpcs {

    public static final ResourceKey<NpcDefinition> solangian_cleric = MKUNpcs.key("solangian_cleric");
    public static final ResourceKey<NpcDefinition> solangian_temple_guard = MKUNpcs.key("solangian_temple_guard");
    public static final ResourceKey<NpcDefinition> solangian_temple_guard_2 = MKUNpcs.key("solangian_temple_guard_2");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(solangian_cleric, generateCleric(solangian_cleric, context));
        context.register(solangian_temple_guard, generateTempleGuard(solangian_temple_guard));
        context.register(solangian_temple_guard_2, generateTempleGuard2(solangian_temple_guard_2));
    }


    static NpcDefinition generateCleric(ResourceKey<NpcDefinition> key, BootstrapContext<NpcDefinition> context) {
        var entitlements = context.lookup(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);

        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
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
                .dialogue(MKUDialogues.cleric_default)
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.MACE_TYPE), 1.0)
                .quests(MKUQuests.CLERIC_UNLOCK_CHAIN)
                .trains(MKUAbilities.HEAL, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ClericTier1)))
                .trains(MKUAbilities.SMITE, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ClericTier1)))
                .trains(MKUAbilities.GALVANIZE, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ClericTier2)))
                .trains(MKUAbilities.POWER_WORD_SUMMON, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ClericTier2)))
                .trains(MKUAbilities.INSPIRE, new HasEntitlementRequirement(entitlements.getOrThrow(MKUEntitlements.ClericTier3)))
                .skillClass(NpcGenUtils.NpcSkillClass.CLERIC)
                .xp(100)
                .build();
    }

    static NpcDefinition generateTempleGuard2(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
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
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.SPEAR_TYPE), 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .xp(65)
                .build();
    }

    static NpcDefinition generateTempleGuard(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
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
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.SPEAR_TYPE), 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .xp(50)
                .build();
    }
}
