package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class SeawovenNpcs {

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
