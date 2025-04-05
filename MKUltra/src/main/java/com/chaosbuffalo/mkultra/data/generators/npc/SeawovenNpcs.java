package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SeawovenNpcs {
    static NpcDefinition generateSeawovenWretch() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("seawoven_wretch"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(MKUSkeletons.SEAWOVEN_WRTECH_NAME));
        def.addOption(new MKSizeOption(0.92f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 35.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 35.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Seawoven Wretch"));
        def.addOption(new AbilitiesOption().withAbilityOption(MKUAbilities.FROZEN_GRASP.get(), 1, 1.0));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.NECROMANCER));
        return def;
    }

    static NpcDefinition generateSeawovenSkeleton() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("seawoven_skeleton"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(MKUSkeletons.SEAWOVEN_NAME));
        def.addOption(new MKSizeOption(0.98f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 45.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 45.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Seawoven Skeleton"));
        def.addOption(new AbilitiesOption().withAbilityOption(MKUAbilities.SEAFURY.get(), 1, 1.0));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.NECROMANCER));
        ResourceLocation lootTierName = MKUltra.id("seawoven_skeleton");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.ITEMS.getName(), lootTierName, 1.0))
                .withDropChances(1)
                .withNoLootChance(0.25)
                .withNoLootIncrease(0.25)
        );
        return def;
    }
}
