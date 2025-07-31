package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.MKULootTiers;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SeawovenNpcs {

    public static final ResourceKey<NpcDefinition> seawoven_wretch = MKUNpcs.key("seawoven_wretch");
    public static final ResourceKey<NpcDefinition> seawoven_skeleton = MKUNpcs.key("seawoven_skeleton");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(seawoven_wretch, generateSeawovenWretch(seawoven_wretch));
        context.register(seawoven_skeleton, generateSeawovenSkeleton(seawoven_skeleton));
    }

    static NpcDefinition generateSeawovenWretch(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
                .faction(MKFactions.UNDEAD)
                .renderGroup(MKUSkeletons.SEAWOVEN_WRTECH_NAME)
                .size(0.92f)
                .attribute(Attributes.MAX_HEALTH, 35.0)
                .attribute(MKAttributes.MAX_MANA, 35.0)
                .attribute(MKAttributes.MANA_REGEN, 2.0)
                .name("A Seawoven Wretch")
                .ability(MKUAbilities.FROZEN_GRASP, 1, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .xp(40)
                .build();
    }

    static NpcDefinition generateSeawovenSkeleton(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HYBOREAN_SKELETON_TYPE)
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
                .xp(50)
                .loot(LootSlotManager.ITEMS, MKULootTiers.seawoven_skeleton, 1.0)
                .build();
    }
}
