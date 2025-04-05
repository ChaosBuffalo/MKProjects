package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.abilities.training.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUEntitlements;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class ClericNpcs {
    static NpcDefinition generateCleric() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_cleric"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.CLERIC_1_NAME));
        def.addOption(new MKSizeOption(1.05f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 500.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 5.0))
        );
        def.addOption(new FactionNameOption().setTitle("Cleric"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.GALVANIZE.get(), 3, 1.0)
                .withAbilityOption(MKUAbilities.POWER_WORD_SUMMON.get(), 4, 1.0)
                .withAbilityOption(MKUAbilities.INSPIRE.get(), 5, 1.0)
        );
        def.addOption(new DialogueOption(MKUltra.id("cleric_default")));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:mace_gold"))), 1.0, 0.0f));
        def.addOption(new QuestOfferingOption(MKUltra.id("cleric_unlock_chain")));
        def.addOption(new AbilityTrainingOption()
                .withTrainingOption(MKUAbilities.HEAL, new HasEntitlementRequirement(MKUEntitlements.ClericTier1.get()))
                .withTrainingOption(MKUAbilities.SMITE, new HasEntitlementRequirement(MKUEntitlements.ClericTier1.get()))
                .withTrainingOption(MKUAbilities.GALVANIZE, new HasEntitlementRequirement(MKUEntitlements.ClericTier2.get()))
                .withTrainingOption(MKUAbilities.POWER_WORD_SUMMON, new HasEntitlementRequirement(MKUEntitlements.ClericTier2.get()))
                .withTrainingOption(MKUAbilities.INSPIRE, new HasEntitlementRequirement(MKUEntitlements.ClericTier3.get()))
        );
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.CLERIC));
        return def;
    }

    static NpcDefinition generateTempleGuard2() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_temple_guard_2"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.TEMPLE_GUARD_2_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 300.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 300.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 4.0))
        );
        def.addOption(new FactionNameOption().setTitle("Temple Guard"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.SEVER_TENDON.get(), 3, 1.0)
        );
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:spear_gold"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.PALADIN));
        return def;
    }

    static NpcDefinition generateTempleGuard() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("solangian_temple_guard"),
                MKUEntities.HUMAN_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.SEE_OF_SOLANG_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.TEMPLE_GUARD_1_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 250.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 250.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new FactionNameOption().setTitle("Temple Guard"));
        def.addOption(new NotableOption());
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
        );
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:spear_gold"))), 1.0, 0.0f));
        def.addOption(equipOption);
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.PALADIN));
        return def;
    }
}
