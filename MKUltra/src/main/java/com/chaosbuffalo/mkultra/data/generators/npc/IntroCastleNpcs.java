package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUPiglins;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.resources.ResourceLocation;

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
}
