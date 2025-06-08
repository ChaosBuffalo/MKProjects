package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUOrcs;
import com.chaosbuffalo.mkultra.init.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class GreenKnightNpcs {

    public static CompletableFuture<?> writeDefinitions(NpcDefinitionProvider provider, CachedOutput cache) {
        return CompletableFuture.allOf(
                provider.writeDefinition(generateGreenLady(), cache),
                provider.writeDefinition(generateGreenLadyGuard1(), cache),
                provider.writeDefinition(generateGreenLadyGuard2(), cache),
                provider.writeDefinition(generateGreenSmith(), cache)
        );
    }

    static NpcDefinition generateGreenLadyGuard1() {
        return new NpcDefinitionBuilder(MKUltra.id("green_lady_guard_1"))
                .type(MKUEntities.ORC_TYPE)
                .faction(MKUFactions.GREEN_KNIGHT_FACTION_NAME)
                .name("Green Knight")
                .size(1.1f)
                .renderGroup(MKUOrcs.GREEN_LADY_GUARD_1_NAME)
                .ability(MKUAbilities.SKIN_LIKE_WOOD, 1, 1.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .ability(MKUAbilities.EXPLOSIVE_GROWTH, 3, 1.0)
                .ability(MKUAbilities.SPIRIT_BOMB, 4, 1.0)
                .ability(MKUAbilities.CLEANSING_SEED, 5, 1.0)
                .health(150)
                .mana(150)
                .manaRegen(4.0)
                .dropChance(0.05f)
                .helmet(MKUItems.greenKnightHelmet)
                .leggings(MKUItems.greenKnightLeggings)
                .chestplate(MKUItems.greenKnightChestplate)
                .boots(MKUItems.greenKnightBoots)
                .dropChance(0.0f)
                .mainHand(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("mkweapons:battleaxe_stone")).get())
                .notable()
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }

    static NpcDefinition generateGreenLadyGuard2() {
        return new NpcDefinitionBuilder(MKUltra.id("green_lady_guard_2"))
                .type(MKUEntities.ORC_TYPE)
                .faction(MKUFactions.GREEN_KNIGHT_FACTION_NAME)
                .name("Green Guardian")
                .size(1.1f)
                .renderGroup(MKUOrcs.GREEN_LADY_GUARD_2_NAME)
                .ability(MKUAbilities.SKIN_LIKE_WOOD, 1, 1.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .ability(MKUAbilities.EXPLOSIVE_GROWTH, 3, 1.0)
                .ability(MKUAbilities.SPIRIT_BOMB, 4, 1.0)
                .ability(MKUAbilities.CLEANSING_SEED, 5, 1.0)
                .health(150)
                .mana(150)
                .manaRegen(4.0)
                .dropChance(0.05f)
                .helmet(MKUItems.greenKnightHelmet)
                .leggings(MKUItems.greenKnightLeggings)
                .chestplate(MKUItems.greenKnightChestplate)
                .boots(MKUItems.greenKnightBoots)
                .dropChance(0.0f)
                .mainHand(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("mkweapons:dagger_stone")).get())
                .notable()
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }

    static NpcDefinition generateGreenLady() {
        return new NpcDefinitionBuilder(MKUltra.id("green_lady"))
                .type(MKUEntities.ORC_TYPE)
                .faction(MKUFactions.GREEN_KNIGHT_FACTION_NAME)
                .name("Green Lady")
                .size(1.1f)
                .renderGroup(MKUOrcs.GREEN_LADY_NAME)
                .ability(MKUAbilities.SKIN_LIKE_WOOD, 1, 1.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .ability(MKUAbilities.EXPLOSIVE_GROWTH, 3, 1.0)
                .ability(MKUAbilities.SPIRIT_BOMB, 4, 1.0)
                .ability(MKUAbilities.CLEANSING_SEED, 5, 1.0)
                .dialogue(MKUltra.id("open_abilities"))
                .health(400)
                .mana(400)
                .manaRegen(10.0)
                .notable()
                .quests(MKUltra.id("intro_quest"))
                .skillClass(NpcGenUtils.NpcSkillClass.CLERIC)
                .trains(MKUAbilities.SKIN_LIKE_WOOD, new HasEntitlementRequirement(MKUEntitlements.GreenKnightTier1.get()))
                .trains(MKUAbilities.NATURES_REMEDY, new HasEntitlementRequirement(MKUEntitlements.GreenKnightTier1.get()))
                .trains(MKUAbilities.SPIRIT_BOMB, new HasEntitlementRequirement(MKUEntitlements.GreenKnightTier2.get()))
                .trains(MKUAbilities.CLEANSING_SEED, new HasEntitlementRequirement(MKUEntitlements.GreenKnightTier2.get()))
                .trains(MKUAbilities.EXPLOSIVE_GROWTH, new HasEntitlementRequirement(MKUEntitlements.GreenKnightTier3.get()))
                .build();
    }

    static NpcDefinition generateGreenSmith() {
        return new NpcDefinitionBuilder(MKUltra.id("green_smith"))
                .type(MKUEntities.ORC_TYPE)
                .faction(MKUFactions.GREEN_KNIGHT_FACTION_NAME)
                .name("Green Smith")
                .size(1.5f)
                .renderGroup(MKUOrcs.GREEN_SMITH_NAME)
                .ability(MKUAbilities.SKIN_LIKE_WOOD, 1, 1.0)
                .ability(MKUAbilities.NATURES_REMEDY, 2, 1.0)
                .health(400)
                .mana(400)
                .manaRegen(4.0)
                .notable()
                .mainHand(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("mkweapons:warhammer_iron")).get())
                .quests(MKUltra.id("trooper_armor"))
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }
}
