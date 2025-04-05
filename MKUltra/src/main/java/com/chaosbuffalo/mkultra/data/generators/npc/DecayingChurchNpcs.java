package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class DecayingChurchNpcs {
    static NpcDefinition generateGhostApprentice() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("ancient_apprentice_ghost"),
                MKUEntities.HUMAN_GHOST_TYPE.getId(), null);
        def.addOption(new FactionOption(MKUFactions.GHOSTS_OF_HYBORIA_NAME));
        def.addOption(new RenderGroupOption(MKUHumans.GHOST_LOOK_CLEAN_SHORT_NAME));
        def.addOption(new MKSizeOption(0.92f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new FactionNameOption().setTitle("Apprentice"));
        def.addOption(new GhostOption().setGhostTranslucency(0.7f));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.SMITE.get(), 2, 1.0)
        );
        def.addOption(new NotableOption());
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.CHEST,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestChestplate.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.LEGS,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestLeggings.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.FEET,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestBoots.get()), 1.0, 0.05f));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.PALADIN));
        def.addOption(equipOption);
        return def;
    }

    static NpcDefinition generateAncientPriestGhost() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("ancient_priest_ghost"),
                MKUEntities.HUMAN_GHOST_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(MKUHumans.GHOST_LOOK_CLEAN_NAME));
        def.addOption(new MKSizeOption(1.0f));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 100.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 3.0))
        );
        def.addOption(new NameOption("An Ancient Priest"));
        def.addOption(new GhostOption().setGhostTranslucency(0.7f));
        def.addOption(new AbilitiesOption()
                .withAbilityOption(MKUAbilities.HEAL.get(), 1, 1.0)
                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 2, 1.0)
                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 3, 0.5)
        );
//        EquipmentOption equipOption = new EquipmentOption();
//        equipOption.addItemChoice(EquipmentSlotType.MAINHAND,
//                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
//                        ResourceLocation.parse("mkweapons:mace_iron"))), 1.0, 0.0f));
//        def.addOption(equipOption);
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.HEAD,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestHelmet.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.CHEST,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestChestplate.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.LEGS,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestLeggings.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.FEET,
                new NpcItemChoice(new ItemStack(MKUItems.ancientPriestBoots.get()), 1.0, 0.05f));
        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.CLERIC));
        def.addOption(equipOption);
        return def;
    }

    static NpcDefinition generateAncientCardinal() {
        NpcDefinition def = new NpcDefinition(MKUltra.id("ancient_cardinal"),
                MKUEntities.HUMAN_GHOST_TYPE.getId(), null);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new MKSizeOption(1.2f));
        def.addOption(new RenderGroupOption(MKUHumans.GHOST_LOOK_CLEAN_NAME));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 350.0))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ARMOR, 20.0))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ATTACK_DAMAGE, 6.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.HOLY_RESISTANCE, 1.25))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MAX_MANA, 350.0))
                .addAttributeEntry(new NpcAttributeEntry(MKAttributes.MANA_REGEN, 6.0))
        );
        def.addOption(new NameOption("Ancient Cardinal"));
        EquipmentOption equipOption = new EquipmentOption();
        equipOption.addItemChoice(EquipmentSlot.MAINHAND,
                new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.parse("mkweapons:warhammer_gold"))), 1.0, 0.0f));
        equipOption.addItemChoice(EquipmentSlot.HEAD,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalHelmet.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.CHEST,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalChestplate.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.LEGS,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalLeggings.get()), 1.0, 0.05f));
        equipOption.addItemChoice(EquipmentSlot.FEET,
                new NpcItemChoice(new ItemStack(MKUItems.ancientCardinalBoots.get()), 1.0, 0.05f));
        def.addOption(equipOption);
        def.addOption(new NotableOption());
        def.addOption(new BossStageOption()
                        .withStage(new BossStage()
                                        .withOption(new TempAbilitiesOption()
                                                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 1, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD_SHOTGUN.get(), 2, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 3, 1.0))
//                        .withOption(new ParticleEffectsOption().withEffects(Collections.singletonList(
//                                new BoneEffectInstance(UUID.fromString("3e7496f1-f5bf-45e6-b8e5-64192633ae9f"),
//                                        BipedSkeleton.HEAD_BONE_NAME, MKUltra.id("flame_wave_casting")))))
                        )
                        .withStage(new BossStage()
                                        .withOption(new TempAbilitiesOption()
                                                .withAbilityOption(MKUAbilities.HOLY_FIRE_FLURRY.get(), 1, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 2, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD_BURST.get(), 3, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD_SHOTGUN.get(), 4, 1.0)
                                                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 5, 1.0))
//                        .withOption(new ParticleEffectsOption().withEffects(Collections.singletonList(
//                                new BoneEffectInstance(UUID.fromString("e45696e1-ddb1-4709-bc29-1733ee1bced9"),
//                                BipedSkeleton.HEAD_BONE_NAME, MKUltra.id("flame_wave_casting")))))
                                        .withParticleMode(BossStage.ParticleMode.LINE_HEIGHT)
                                        .withTransitionParticles(MKUltra.id("wrath_skeleton_transition"))
                                        .withTransitionSound(MKUSounds.spell_holy_9.getId())
                        )
        );
        def.addOption(new ExperienceOption(75));
//        def.addOption(new ParticleEffectsOption(List.of(
//                new BoneEffectInstance(UUID.fromString("3e7496f1-f5bf-45e6-b8e5-64192633ae9f"),
//                        MKUltra.id("burning_skeleton_head"), BipedSkeleton.HEAD_BONE_NAME)
//        )));
//        ResourceLocation lootTierName = MKUltra.id("burning_skeleton");
//        def.addOption(new ExtraLootOption().withLootOptions(new LootOptionEntry(LootSlotManager.MAIN_HAND.getName(), lootTierName, 1.0))
//                .withLootOptions(new LootOptionEntry(LootSlotManager.RINGS.getName(), lootTierName, 3.0))
//                .withLootOptions(new LootOptionEntry(LootSlotManager.EARRINGS.getName(), lootTierName, 2.0))
//                .withDropChances(1)
//                .withNoLootChance(0.1)
//                .withNoLootIncrease(0.0));

        def.addOption(NpcGenUtils.getSkillOptionForClass(NpcGenUtils.NpcSkillClass.MAGE));
        return def;
    }
}
