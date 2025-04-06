package com.chaosbuffalo.mknpc.data;


import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class NpcDefinitionBuilder {
    private final ResourceLocation name;
    private ResourceLocation parentName;
    private Holder<EntityType<?>> entityType;
    private final Map<ResourceLocation, NpcDefinitionOption> options = new HashMap<>();
    private float defaultDropChance;

    public NpcDefinitionBuilder(ResourceLocation name) {
        this.name = name;
        defaultDropChance = 0.0f;
    }

    public NpcDefinitionBuilder type(Holder<EntityType<?>> entityType) {
        this.entityType = entityType;
        return this;
    }

    public NpcDefinitionBuilder parent(ResourceLocation parentName) {
        this.parentName = parentName;
        return this;
    }

    public NpcDefinitionBuilder faction(ResourceKey<MKFaction> faction) {
        var opt = new FactionOption(faction);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder attribute(Holder<Attribute> attribute, double value) {
        AttributesOption opt = (AttributesOption) options.computeIfAbsent(AttributesOption.NAME,
                key -> new AttributesOption());
        opt.addAttributeEntry(new NpcAttributeEntry(attribute, value));
        return this;
    }

    public NpcDefinitionBuilder name(String name) {
        var opt = new NameOption(name);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder factionName(boolean hasLastName) {
        var opt = new FactionNameOption();
        opt.setHasLastName(hasLastName);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder factionName(){
        return factionName(false);
    }

    public NpcDefinitionBuilder titledFactionName(String title, boolean hasLastName) {
        var opt = new FactionNameOption();
        opt.setHasLastName(hasLastName);
        opt.setTitle(title);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder renderGroup(String groupName) {
        var opt = new RenderGroupOption(groupName);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder size(float value) {
        var opt = new MKSizeOption(value);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder equip(EquipmentSlot slot, ItemStack stack, double weight, float dropChance) {
        EquipmentOption opt = (EquipmentOption) options.computeIfAbsent(EquipmentOption.NAME,
                key -> new EquipmentOption());
        opt.addItemChoice(slot, new NpcItemChoice(stack, weight, dropChance));
        return this;
    }

    public NpcDefinitionBuilder equip(EquipmentSlot slot, Holder<Item> item, double weight, float dropChance) {
        return equip(slot, new ItemStack(item.value()), weight, dropChance);
    }

    public NpcDefinitionBuilder dropChance(float chance) {
        this.defaultDropChance = chance;
        return this;
    }

    public NpcDefinitionBuilder equip(EquipmentSlot slot, Holder<Item> item) {
        return equip(slot, item, 1.0, defaultDropChance);
    }

    public NpcDefinitionBuilder mainHand(Holder<Item> item) {
        return equip(EquipmentSlot.MAINHAND, item);
    }

    public NpcDefinitionBuilder mainHand(Holder<Item> item, double weight) {
        return equip(EquipmentSlot.MAINHAND, item, weight, defaultDropChance);
    }

    public NpcDefinitionBuilder offHand(Holder<Item> item) {
        return equip(EquipmentSlot.OFFHAND, item);
    }

    public NpcDefinitionBuilder offHand(Holder<Item> item, double weight) {
        return equip(EquipmentSlot.OFFHAND, item, weight, defaultDropChance);
    }

    public NpcDefinitionBuilder helmet(Holder<Item> item) {
        return equip(EquipmentSlot.HEAD, item);
    }

    public NpcDefinitionBuilder chestplate(Holder<Item> item) {
        return equip(EquipmentSlot.CHEST, item);
    }

    public NpcDefinitionBuilder leggings(Holder<Item> item) {
        return equip(EquipmentSlot.LEGS, item);
    }

    public NpcDefinitionBuilder boots(Holder<Item> item) {
        return equip(EquipmentSlot.FEET, item);
    }

    public NpcDefinitionBuilder emptyChance(EquipmentSlot slot, double weight) {
        return equip(slot, ItemStack.EMPTY, weight, 0.0f);
    }

    public NpcDefinitionBuilder notable() {
        var opt = new NotableOption();
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder ghost(float translucency) {
        GhostOption opt = (GhostOption) options.computeIfAbsent(GhostOption.NAME,
                key -> new GhostOption());
        opt.setGhostTranslucency(translucency);
        return this;
    }

    public NpcDefinitionBuilder ghost(float translucency, float armorTranslucency) {
        GhostOption opt = (GhostOption) options.computeIfAbsent(GhostOption.NAME,
                key -> new GhostOption());
        opt.setGhostTranslucency(translucency);
        opt.setArmorTranslucency(armorTranslucency);
        return this;
    }

    public NpcDefinitionBuilder titledFactionName(String title) {
        return titledFactionName(title, false);
    }

    public NpcDefinitionBuilder ability(Holder<MKAbility> ability, int priority, double chance) {
        AbilitiesOption opt = (AbilitiesOption) options.computeIfAbsent(AbilitiesOption.NAME,
                key -> new AbilitiesOption());
        opt.withAbilityOption(ability.value(), priority, chance);
        return this;
    }

    public NpcDefinitionBuilder skillClass(NpcGenUtils.NpcSkillClass skillClass) {
        var opt = NpcGenUtils.getSkillOptionForClass(skillClass);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder battlecry() {
        var opt = new FactionBattlecryOption();
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder quests(ResourceLocation... questIds) {
        var opt = new QuestOfferingOption(Arrays.stream(questIds).toList());
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder dialogue(ResourceLocation dialogueId) {
        var opt = new DialogueOption(dialogueId);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder trains(Holder<MKAbility> ability, AbilityTrainingRequirement... requirements) {
        AbilityTrainingOption opt = (AbilityTrainingOption) options.computeIfAbsent(AbilityTrainingOption.NAME,
                key -> new AbilityTrainingOption());
        opt.withTrainingOption(ability, requirements);
        return this;
    }

    public NpcDefinitionBuilder health(double value){
        return attribute(Attributes.MAX_HEALTH, value);
    }

    public NpcDefinitionBuilder mana(double value) {
        return attribute(MKAttributes.MAX_MANA, value);
    }

    public NpcDefinitionBuilder manaRegen(double value) {
        return attribute(MKAttributes.MANA_REGEN, value);
    }

    public NpcDefinitionBuilder combo(int delay, int count) {
        var opt = new MKComboSettingsOption(delay, count);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder loot(LootSlot slot, ResourceLocation lootTier, double weight) {
        ExtraLootOption opt = (ExtraLootOption) options.computeIfAbsent(ExtraLootOption.NAME,
                key -> new ExtraLootOption());
        opt.withLootOptions(new LootOptionEntry(slot.getName(), lootTier, weight));
        return this;
    }

    public NpcDefinitionBuilder lootDropChances(int chances) {
        ExtraLootOption opt = (ExtraLootOption) options.computeIfAbsent(ExtraLootOption.NAME,
                key -> new ExtraLootOption());
        opt.withDropChances(chances);
        return this;
    }

    public NpcDefinitionBuilder noLootChance(double chance) {
        ExtraLootOption opt = (ExtraLootOption) options.computeIfAbsent(ExtraLootOption.NAME,
                key -> new ExtraLootOption());
        opt.withNoLootChance(chance);
        return this;
    }

    public NpcDefinitionBuilder noLootChanceIncrease(double chance) {
        ExtraLootOption opt = (ExtraLootOption) options.computeIfAbsent(ExtraLootOption.NAME,
                key -> new ExtraLootOption());
        opt.withNoLootIncrease(chance);
        return this;
    }

    public NpcDefinitionBuilder lungeSpeed(double speed) {
        var opt = new LungeSpeedOption(speed);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder xp(int value) {
        var opt = new ExperienceOption(value);
        index(opt);
        return this;
    }

    public NpcDefinitionBuilder bossStage(BossStage stage) {
        BossStageOption opt = (BossStageOption) options.computeIfAbsent(BossStageOption.NAME,
                key -> new BossStageOption());
        opt.withStage(stage);
        return this;
    }

    public NpcDefinitionBuilder particles(ParticleEffectInstance... instances) {
        ParticleEffectsOption opt = new ParticleEffectsOption(Arrays.stream(instances).toList());
        index(opt);
        return this;
    }

    protected void index(NpcDefinitionOption option) {
        options.put(option.getName(), option);
    }

    public NpcDefinition build() {
        if (parentName == null && entityType == null) {
            throw new IllegalArgumentException("You must specify either parent or entity type");
        }
        var def = new NpcDefinition(name, entityType.getKey().location(), parentName);
        for (var entry : options.entrySet()) {
            def.addOption(entry.getValue());
        }
        return def;
    }
}
