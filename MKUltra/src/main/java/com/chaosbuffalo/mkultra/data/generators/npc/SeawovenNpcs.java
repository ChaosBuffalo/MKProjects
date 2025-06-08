package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.concurrent.CompletableFuture;

public class SeawovenNpcs {

    public static CompletableFuture<?> writeDefinitions(NpcDefinitionProvider provider, CachedOutput cache) {
        return CompletableFuture.allOf(
                provider.writeDefinition(generateSeawovenSkeleton(), cache),
                provider.writeDefinition(generateSeawovenWretch(), cache)
        );
    }

    static NpcDefinition generateSeawovenWretch() {
        return new NpcDefinitionBuilder(MKUltra.id("seawoven_wretch"))
                .type(MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKFactions.UNDEAD)
                .renderGroup(MKUSkeletons.SEAWOVEN_WRTECH_NAME)
                .size(0.92f)
                .attribute(Attributes.MAX_HEALTH, 35.0)
                .attribute(MKAttributes.MAX_MANA, 35.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("A Seawoven Wretch")
                .ability(MKUAbilities.FROZEN_GRASP, 1, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .build();
    }

    static NpcDefinition generateSeawovenSkeleton() {
        ResourceLocation lootTierName = MKUltra.id("seawoven_skeleton");
        return new NpcDefinitionBuilder(MKUltra.id("seawoven_skeleton"))
                .type(MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKFactions.UNDEAD)
                .renderGroup(MKUSkeletons.SEAWOVEN_NAME)
                .size(0.98f)
                .attribute(Attributes.MAX_HEALTH, 45.0)
                .attribute(MKAttributes.MAX_MANA, 45.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("A Seawoven Skeleton")
                .ability(MKUAbilities.SEAFURY, 1, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .lootDropChances(1)
                .noLootChanceIncrease(.25)
                .noLootChance(.25)
                .loot(LootSlotManager.ITEMS, lootTierName, 1.0)
                .build();
    }
}
