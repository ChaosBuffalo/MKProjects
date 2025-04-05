package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUGolems;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.client.render.styling.MKUSkeletons;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class NecrotideNpcs {
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
                .battlecry()
                .build();
    }

    static NpcDefinition generateSkeletalLock() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("skeletal_lock"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new RenderGroupOption(MKUSkeletons.BASIC_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Skeletal Lock"));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.SHADOW_BOLT.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SHADOW_PULSE.get(), 2, 1.0)
        );
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        def.addOption(new NotableOption());
        return def;
    }

    static NpcDefinition generateNecrotideGolem() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_golem"),
                MKUEntities.GOLEM_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new RenderGroupOption(MKUGolems.NECROTIDE_GOLEM_NAME));
        def.addOption(new MKSizeOption(1.25f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 5.0))
        );
        def.addOption(new NameOption("A Necrotide Construction"));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.SHADOW_BOLT.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.NECROTIDE_GOLEM_BEAM.get(), 1, 1.0)
        );
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.NECROMANCER));
        def.addOption(new NotableOption());
        ResourceLocation lootTierName = MKUltra.id("necrotide_golem");
        def.addOption(new ExtraLootOption()
                .withLootOptions(new LootOptionEntry(LootSlotManager.RINGS.getName(), lootTierName, 3.0))
                .withLootOptions(new LootOptionEntry(LootSlotManager.HANDS.getName(), lootTierName, 1.0))
                .withDropChances(1)
                .withNoLootChance(0.0)
                .withNoLootIncrease(0.0));
        return def;
    }

    static NpcDefinition generateNecrotideCultistAcolyte() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_acolyte"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.NECROTIDE_CULTIST_1_NAME));
        def.addOption(new MKSizeOption(0.95f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 50.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("A Necrotide Acolyte"));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.SHADOW_BOLT.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.DROWN.get(), 2, 0.5)
                .withAbilityOption(MKUAbilities.SHADOW_PULSE.get(), 3, 0.5)
        );
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        def.addOption(new FactionBattlecryOption());
        return def;
    }

    static NpcDefinition generateNecrotideSkeletalWarrior() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_skeletal_warrior"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new RenderGroupOption(MKUSkeletons.BASIC_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 65.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 65.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 2.0))
        );
        def.addOption(new NameOption("Skeleton Warrior"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:battleaxe_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:spear_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:greatsword_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:longsword_iron"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:warhammer_iron"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.WARRIOR));
        return def;
    }

    static NpcDefinition generateNecrotideSkeletalArcher() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("necrotide_skeletal_archer"),
                MKUEntities.HYBOREAN_SKELETON_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.NECROTIDE_CULTISTS_NAME));
        def.addOption(new MKSizeOption(0.95f));
        def.addOption(new RenderGroupOption(MKUSkeletons.HYBOREAN_ARCHER_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 30.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 30.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 1.0))
        );
        def.addOption(new NameOption("Skeleton Archer"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:longbow_iron"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.ARCHER));
        return def;
    }
}
