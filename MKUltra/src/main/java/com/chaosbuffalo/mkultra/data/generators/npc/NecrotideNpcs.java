package com.chaosbuffalo.mkultra.data.generators.npc;


import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.client.render.styling.MKUGolems;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.MKULootTiers;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.chaosbuffalo.mkweapons.items.weapon.types.RangedWeaponTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NecrotideNpcs {

    public static final ResourceKey<NpcDefinition> necrotide_cultist = MKUNpcs.key("necrotide_cultist");
    public static final ResourceKey<NpcDefinition> skeletal_lock = MKUNpcs.key("skeletal_lock");
    public static final ResourceKey<NpcDefinition> necrotide_golem = MKUNpcs.key("necrotide_golem");
    public static final ResourceKey<NpcDefinition> necrotide_acolyte = MKUNpcs.key("necrotide_acolyte");
    public static final ResourceKey<NpcDefinition> necrotide_skeletal_warrior = MKUNpcs.key("necrotide_skeletal_warrior");
    public static final ResourceKey<NpcDefinition> necrotide_skeletal_archer = MKUNpcs.key("necrotide_skeletal_archer");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(necrotide_cultist, generateNecrotideCultist(necrotide_cultist));
        context.register(skeletal_lock, generateSkeletalLock(skeletal_lock));
        context.register(necrotide_golem, generateNecrotideGolem(necrotide_golem));
        context.register(necrotide_acolyte, generateNecrotideCultistAcolyte(necrotide_acolyte));
        context.register(necrotide_skeletal_warrior, generateNecrotideSkeletalWarrior(necrotide_skeletal_warrior));
        context.register(necrotide_skeletal_archer, generateNecrotideSkeletalArcher(necrotide_skeletal_archer));
    }

    static NpcDefinition generateNecrotideCultist(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
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

    static NpcDefinition generateSkeletalLock(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
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

    static NpcDefinition generateNecrotideGolem(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.GOLEM_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUGolems.NECROTIDE_GOLEM_NAME)
                .size(1.25f)
                .attribute(Attributes.MAX_HEALTH, 750.0)
                .attribute(MKAttributes.MAX_MANA, 750.0)
                .attribute(MKAttributes.MANA_REGEN, 5.0)
                .name("A Necrotide Construction")
                .ability(MKUAbilities.SHADOW_PUlSE_FLURRY, 3, 1.0)
                .ability(MKUAbilities.SHADOW_BOLT, 2, 1.0)
                .ability(MKUAbilities.NECROTIDE_GOLEM_BEAM, 1, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .notable()
                .loot(LootSlotManager.RINGS, MKULootTiers.necrotide_golem, 3.0)
                .loot(LootSlotManager.HANDS, MKULootTiers.necrotide_golem, 1.0)
                .lootDropChances(1)
                .noLootChance(0.0)
                .noLootChanceIncrease(0.0)
                .xp(500)
                .build();
    }

    static NpcDefinition generateNecrotideCultistAcolyte(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_TYPE)
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

    static NpcDefinition generateNecrotideSkeletalWarrior(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUSkeletons.BASIC_NAME)
                .size(1.0f)
                .attribute(Attributes.MAX_HEALTH, 65.0)
                .attribute(MKAttributes.MAX_MANA, 65.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("Skeleton Warrior")
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.SPEAR_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.GREATSWORD_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.LONGSWORD_TYPE))
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.WARHAMMER_TYPE))
                .skillClass(NpcGenUtils.NpcSkillClass.WARRIOR)
                .xp(30)
                .build();
    }

    static NpcDefinition generateNecrotideSkeletalArcher(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUSkeletons.HYBOREAN_ARCHER_NAME)
                .size(0.95f)
                .attribute(Attributes.MAX_HEALTH, 30.0)
                .attribute(MKAttributes.MAX_MANA, 30.0)
                .attribute(MKAttributes.MANA_REGEN, 1.0)
                .name("Skeleton Archer")
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, RangedWeaponTypes.LONGBOW))
                .skillClass(NpcGenUtils.NpcSkillClass.ARCHER)
                .xp(25)
                .build();
    }
}
