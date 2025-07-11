package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.talents.TalentLineDefinition;
import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.nodes.AbilityGrantTalentNode;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;
import com.chaosbuffalo.mkcore.init.CoreTalentDisplayNodes;
import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MKUTalentTrees {
    public record Template(Holder<Attribute> attribute, ResourceKey<TalentNodeDisplay> displayKey) {
    }

    static Template MELEE_CRIT_MULTIPLIER = new Template(
            MKAttributes.MELEE_CRIT_MULTIPLIER,
            CoreTalentDisplayNodes.MELEE_CRIT_MULTIPLIER
    );
    static Template MELEE_CRIT = new Template(
            MKAttributes.MELEE_CRIT,
            CoreTalentDisplayNodes.MELEE_CRIT
    );
    static Template MAX_MANA = new Template(
            MKAttributes.MAX_MANA,
            CoreTalentDisplayNodes.MAX_MANA
    );
    static Template HEAL_BONUS = new Template(
            MKAttributes.HEAL_BONUS,
            CoreTalentDisplayNodes.HEAL_BONUS
    );
    static Template MOVEMENT_SPEED = new Template(
            Attributes.MOVEMENT_SPEED,
            CoreTalentDisplayNodes.MOVEMENT_SPEED
    );
    static Template COOLDOWN_REDUCTION = new Template(
            MKAttributes.COOLDOWN,
            CoreTalentDisplayNodes.COOLDOWN_REDUCTION
    );
    static Template MAX_HEALTH = new Template(
            Attributes.MAX_HEALTH,
            CoreTalentDisplayNodes.MAX_HEALTH
    );
    static Template ARMOR = new Template(
            Attributes.ARMOR,
            CoreTalentDisplayNodes.ARMOR
    );
    static Template HEALTH_REGEN = new Template(
            MKAttributes.HEALTH_REGEN,
            CoreTalentDisplayNodes.HEALTH_REGEN
    );
    static Template MANA_REGEN = new Template(
            MKAttributes.MANA_REGEN,
            CoreTalentDisplayNodes.MANA_REGEN
    );
    static Template SPELL_CRIT = new Template(
            MKAttributes.SPELL_CRIT,
            CoreTalentDisplayNodes.SPELL_CRIT
    );
    static Template SPELL_CRIT_MULTIPLIER = new Template(
            MKAttributes.SPELL_CRIT_MULTIPLIER,
            CoreTalentDisplayNodes.SPELL_CRIT_MULTIPLIER
    );
    static Template HOLY_DAMAGE = new Template(
            MKAttributes.HOLY_DAMAGE,
            CoreTalentDisplayNodes.HOLY_DAMAGE
    );
    static Template NATURE_DAMAGE = new Template(
            MKAttributes.NATURE_DAMAGE,
            CoreTalentDisplayNodes.NATURE_DAMAGE
    );
    static Template FIRE_DAMAGE = new Template(
            MKAttributes.FIRE_DAMAGE,
            CoreTalentDisplayNodes.FIRE_DAMAGE
    );
    static Template FIRE_RESIST = new Template(
            MKAttributes.FIRE_RESISTANCE,
            CoreTalentDisplayNodes.FIRE_RESISTANCE
    );
    static Template FROST_DAMAGE = new Template(
            MKAttributes.FROST_DAMAGE,
            CoreTalentDisplayNodes.FROST_DAMAGE
    );
    static Template FROST_RESIST = new Template(
            MKAttributes.FROST_RESISTANCE,
            CoreTalentDisplayNodes.FROST_RESISTANCE
    );
    static Template ARCANE_DAMAGE = new Template(
            MKAttributes.ARCANE_DAMAGE,
            CoreTalentDisplayNodes.ARCANE_DAMAGE
    );
    static Template ARCANE_RESIST = new Template(
            MKAttributes.ARCANE_RESISTANCE,
            CoreTalentDisplayNodes.ARCANE_RESISTANCE
    );
    static Template BLEED_DAMAGE = new Template(
            MKAttributes.BLEED_DAMAGE,
            CoreTalentDisplayNodes.BLEED_DAMAGE
    );
    static Template BLEED_RESIST = new Template(
            MKAttributes.BLEED_RESISTANCE,
            CoreTalentDisplayNodes.BLEED_RESISTANCE
    );
    static Template MAX_POISE = new Template(
            MKAttributes.MAX_POISE,
            CoreTalentDisplayNodes.MAX_POISE
    );
    static Template POISE_REGEN = new Template(
            MKAttributes.POISE_REGEN,
            CoreTalentDisplayNodes.POISE_REGEN
    );
    static Template POISE_BREAK_CD = new Template(
            MKAttributes.POISE_BREAK_CD,
            CoreTalentDisplayNodes.POISE_BREAK_CD
    );
    static Template HEAL_EFFICIENCY = new Template(
            MKAttributes.HEAL_EFFICIENCY,
            CoreTalentDisplayNodes.HEAL_EFFICIENCY
    );
    static Template ATTACK_DAMAGE = new Template(
            Attributes.ATTACK_DAMAGE,
            CoreTalentDisplayNodes.ATTACK_DAMAGE
    );

    public static ResourceKey<TalentTreeDefinition> key(ResourceLocation id) {
        return ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, id);
    }

    public static final ResourceKey<TalentTreeDefinition> WARRIOR = key(MKUltra.id("warrior"));
    public static final ResourceKey<TalentTreeDefinition> PRIEST = key(MKUltra.id("priest"));
    public static final ResourceKey<TalentTreeDefinition> MAGE = key(MKUltra.id("mage"));
    public static final ResourceKey<TalentTreeDefinition> KNIGHT = key(MKUltra.id("knight"));
    public static final ResourceKey<TalentTreeDefinition> GREEN_KNIGHT = key(MKUltra.id("green_knight"));
    public static final ResourceKey<TalentTreeDefinition> KNAVE = key(MKUltra.id("knave"));
    public static final ResourceKey<TalentTreeDefinition> CORE = key(MKUltra.id("core"));

    static Component defaultName(ResourceKey<TalentTreeDefinition> treeId) {
        return Component.translatable(TalentTreeDefinition.nameKey(treeId.location()));
    }

    static Holder<TalentNodeDisplay> nodeDisplay(BootstrapContext<TalentTreeDefinition> context, ResourceKey<TalentNodeDisplay> displayKey) {
        return context.lookup(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY).getOrThrow(displayKey);
    }

    static AttributeTalentNode attrNode(BootstrapContext<TalentTreeDefinition> context, Template template, int maxRanks, double perRank) {
        var displayNode = nodeDisplay(context, template.displayKey());
        return new AttributeTalentNode(template.attribute(), displayNode, maxRanks, perRank, AttributeModifier.Operation.ADD_VALUE);
    }

    private static @NotNull TalentTreeDefinition makeWarriorTree(BootstrapContext<TalentTreeDefinition> context) {
        TalentTreeDefinition tree = new TalentTreeDefinition(defaultName(WARRIOR));
        tree.setVersion(1);
        tree.setDefault(true);

        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        aLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 2, 0.1));
        aLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        aLine.addNode(attrNode(context, ARMOR, 1, 1));
        aLine.addNode(attrNode(context, MOVEMENT_SPEED, 2, 0.01));
        aLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 2, 0.1));
        // FIXME: Dual wield was here
        tree.addLine(aLine);

        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        bLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 2, 0.1));
        bLine.addNode(attrNode(context, MELEE_CRIT, 3, 0.01));
        bLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 2, 0.1));
        bLine.addNode(attrNode(context, MELEE_CRIT, 2, 0.01));
        bLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        bLine.addNode(attrNode(context, MOVEMENT_SPEED, 3, 0.01));
        bLine.addNode(attrNode(context, HEALTH_REGEN, 2, 0.25));
        bLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 3, 0.1));
        // FIXME: bolstering roar was here
        tree.addLine(bLine);

        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        cLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 2, 0.1));
        cLine.addNode(attrNode(context, HEALTH_REGEN, 2, 0.25));
        cLine.addNode(attrNode(context, ARMOR, 1, 1));
        cLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        cLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 2, 0.1));
        // FIXME: blademaster went here
        tree.addLine(cLine);
        return tree;
    }

    private static @NotNull TalentTreeDefinition makePriestTree(BootstrapContext<TalentTreeDefinition> context) {
        TalentTreeDefinition tree = new TalentTreeDefinition(defaultName(PRIEST));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        aLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        aLine.addNode(attrNode(context, HEAL_BONUS, 1, 1));
        aLine.addNode(attrNode(context, MOVEMENT_SPEED, 3, 0.01));
        aLine.addNode(attrNode(context, COOLDOWN_REDUCTION, 2, 0.025));
        aLine.addNode(attrNode(context, HOLY_DAMAGE, 5, 1));
        // FIXME: Armor Training here
        tree.addLine(aLine);

        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        bLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        bLine.addNode(attrNode(context, COOLDOWN_REDUCTION, 2, 0.025));
        bLine.addNode(attrNode(context, HEAL_BONUS, 1, 1));
        bLine.addNode(attrNode(context, MANA_REGEN, 4, 0.25));
        bLine.addNode(attrNode(context, COOLDOWN_REDUCTION, 2, 0.025));
        bLine.addNode(attrNode(context, SPELL_CRIT, 2, 0.01));
        bLine.addNode(attrNode(context, HEAL_BONUS, 2, 1));
        bLine.addNode(attrNode(context, COOLDOWN_REDUCTION, 2, 0.025));
        // FIXME: Healing Rain
        tree.addLine(bLine);

        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        cLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        cLine.addNode(attrNode(context, MANA_REGEN, 2, .25));
        cLine.addNode(attrNode(context, NATURE_DAMAGE, 5, 1));
        cLine.addNode(attrNode(context, MANA_REGEN, 4, 0.25));
        cLine.addNode(attrNode(context, HEAL_BONUS, 1, 1));
        // FIXME: Guardian Angle
        tree.addLine(cLine);
        return tree;
    }

    private static @NotNull TalentTreeDefinition makeMageTree(BootstrapContext<TalentTreeDefinition> context) {
        TalentTreeDefinition tree = new TalentTreeDefinition(defaultName(MAGE));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        aLine.addNode(attrNode(context, SPELL_CRIT, 2, 0.01));
        aLine.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        aLine.addNode(attrNode(context, FIRE_DAMAGE, 5, 1));
        aLine.addNode(attrNode(context, FIRE_RESIST, 5, 0.01));
        aLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        aLine.addNode(attrNode(context, MOVEMENT_SPEED, 2, 0.01));
        aLine.addNode(attrNode(context, SPELL_CRIT_MULTIPLIER, 2, 0.2));
        // FIXME: Burning Soul here
        tree.addLine(aLine);

        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        bLine.addNode(attrNode(context, ARCANE_DAMAGE, 5, 1));
        bLine.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        bLine.addNode(attrNode(context, SPELL_CRIT_MULTIPLIER, 2, 0.2));
        bLine.addNode(attrNode(context, SPELL_CRIT, 2, 0.01));
        bLine.addNode(attrNode(context, SPELL_CRIT_MULTIPLIER, 2, 0.2));
        bLine.addNode(attrNode(context, ARCANE_RESIST, 5, 0.01));
        bLine.addNode(attrNode(context, SPELL_CRIT_MULTIPLIER, 2, 0.2));
        // FIXME: Meteor here
        tree.addLine(bLine);

        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        cLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        cLine.addNode(attrNode(context, MANA_REGEN, 4, 0.25));
        cLine.addNode(attrNode(context, FROST_DAMAGE, 5, 1));
        cLine.addNode(attrNode(context, FROST_RESIST, 5, 0.01));
        cLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        cLine.addNode(attrNode(context, SPELL_CRIT, 2, 0.01));
        cLine.addNode(attrNode(context, SPELL_CRIT_MULTIPLIER, 2, 0.2));
        // FIXME: Soul drain here
        tree.addLine(cLine);
        return tree;
    }

    private static @NotNull TalentTreeDefinition makeKnightTree(BootstrapContext<TalentTreeDefinition> context) {
        TalentTreeDefinition tree = new TalentTreeDefinition(defaultName(KNIGHT));
        tree.setVersion(1);
        tree.setDefault(true);

        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        aLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        aLine.addNode(attrNode(context, ARMOR, 2, 1));
        aLine.addNode(attrNode(context, MAX_POISE, 2, 2.0));
        aLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        aLine.addNode(attrNode(context, HEALTH_REGEN, 2, 0.25));
        // FIXME: 2 handed style
        tree.addLine(aLine);

        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        bLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        bLine.addNode(attrNode(context, HEALTH_REGEN, 4, 0.25));
        bLine.addNode(attrNode(context, MAX_HEALTH, 4, 1));
        bLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        bLine.addNode(attrNode(context, HEAL_BONUS, 3, 1));
        bLine.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        bLine.addNode(attrNode(context, ARCANE_RESIST, 3, 0.01));
        bLine.addNode(attrNode(context, ARMOR, 4, 1));
        bLine.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        // FIXME: Righteous judgement
        tree.addLine(bLine);

        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        cLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        cLine.addNode(attrNode(context, ARCANE_RESIST, 2, 0.01));
        cLine.addNode(attrNode(context, HEALTH_REGEN, 2, 0.25));
        cLine.addNode(attrNode(context, ARMOR, 2, 1));
        cLine.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        // FIXME: holy aura
        tree.addLine(cLine);
        return tree;
    }

    private static @NotNull TalentTreeDefinition makeGreenKnightTree(BootstrapContext<TalentTreeDefinition> context) {
        TalentTreeDefinition tree = new TalentTreeDefinition(defaultName(GREEN_KNIGHT));
        tree.setVersion(1);
        TalentLineDefinition line = new TalentLineDefinition(tree, "a");
        line.addNode(attrNode(context, MAX_HEALTH, 3, 4.0));
        line.addNode(attrNode(context, MAX_POISE, 2, 2.0));
        line.addNode(attrNode(context, POISE_REGEN, 2, 0.25));
        line.addNode(attrNode(context, POISE_BREAK_CD, 2, -0.5));
        line.addNode(attrNode(context, POISE_REGEN, 2, 0.25));
        tree.addLine(line);

        TalentLineDefinition soul = new TalentLineDefinition(tree, "b");
        soul.addNode(attrNode(context, MAX_HEALTH, 3, 4.0));
        soul.addNode(new EntitlementGrantTalentNode(CoreEntitlements.ABILITY_POOL_SIZE,
                nodeDisplay(context, CoreTalentDisplayNodes.POOL_COUNT),
                UUID.fromString("40525592-c013-46f7-84da-1543d5a28cfc")));
        soul.addNode(attrNode(context, MAX_POISE, 3, 2.0));
        soul.addNode(attrNode(context, NATURE_DAMAGE, 5, 1));
        soul.addNode(attrNode(context, HEAL_EFFICIENCY, 5, 0.02));
        soul.addNode(attrNode(context, ARMOR, 5, 1.0));
        soul.addNode(new AbilityGrantTalentNode(MKUAbilities.GREEN_SOUL,
                nodeDisplay(context, MKUTalentDisplayNodes.GREEN_SOUL)));
        tree.addLine(soul);

        TalentLineDefinition healing = new TalentLineDefinition(tree, "c");
        healing.addNode(attrNode(context, MAX_HEALTH, 3, 4.0));
        healing.addNode(attrNode(context, HEAL_BONUS, 2, 1.0));
        healing.addNode(attrNode(context, MAX_MANA, 3, 2.0));
        healing.addNode(attrNode(context, MAX_POISE, 2, 2.0));
        healing.addNode(attrNode(context, MANA_REGEN, 1, 0.5));
        tree.addLine(healing);
        return tree;
    }

    private static @NotNull TalentTreeDefinition makeKnaveTree(BootstrapContext<TalentTreeDefinition> context) {
        TalentTreeDefinition tree = new TalentTreeDefinition(defaultName(KNAVE));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        aLine.addNode(attrNode(context, BLEED_DAMAGE, 1, 1));
        aLine.addNode(attrNode(context, BLEED_RESIST, 3, 0.01));
        aLine.addNode(attrNode(context, MELEE_CRIT, 2, 0.01));
        aLine.addNode(attrNode(context, BLEED_DAMAGE, 1, 1));
        aLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        // FIXME: life siphon used to be here
        tree.addLine(aLine);

        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        bLine.addNode(attrNode(context, ATTACK_DAMAGE, 1, 1));
        bLine.addNode(attrNode(context, MAX_HEALTH, 2, 1));
        bLine.addNode(attrNode(context, MOVEMENT_SPEED, 3, 0.01));
        bLine.addNode(attrNode(context, ATTACK_DAMAGE, 2, 1));
        bLine.addNode(attrNode(context, MOVEMENT_SPEED, 4, 0.01));
        bLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        bLine.addNode(attrNode(context, ATTACK_DAMAGE, 3, 1));
        bLine.addNode(attrNode(context, ARMOR, 1, 1));
        bLine.addNode(attrNode(context, MELEE_CRIT_MULTIPLIER, 5, 0.1));
        // FIXME: backstab
        tree.addLine(bLine);

        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        cLine.addNode(attrNode(context, MAX_MANA, 2, 1));
        cLine.addNode(attrNode(context, SPELL_CRIT, 2, 0.01));
        cLine.addNode(attrNode(context, MOVEMENT_SPEED, 3, 0.01));
        cLine.addNode(attrNode(context, BLEED_DAMAGE, 1, 1));
        cLine.addNode(attrNode(context, BLEED_RESIST, 2, 0.01));
        cLine.addNode(attrNode(context, MELEE_CRIT, 3, 0.01));
        tree.addLine(cLine);
        return tree;
    }

    private static @NotNull TalentTreeDefinition makeCoreTree(BootstrapContext<TalentTreeDefinition> context) {
        TalentTreeDefinition tree = new TalentTreeDefinition(defaultName(CORE));
        tree.setVersion(2);
        tree.setDefault(true);

        TalentLineDefinition line = new TalentLineDefinition(tree, "a");
        line.addNode(new EntitlementGrantTalentNode(CoreEntitlements.BASIC_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.ABILITY_SLOT),
                UUID.fromString("119917ea-b852-4cb5-8bfe-2cdad488f279")));
        line.addNode(attrNode(context, MAX_HEALTH, 2, 5.0));
        line.addNode(attrNode(context, MAX_MANA, 2, 1.0));
        line.addNode(new EntitlementGrantTalentNode(CoreEntitlements.BASIC_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.ABILITY_SLOT),
                UUID.fromString("121817fa-1cfc-4334-aa77-13c02ede83ff")));
        line.addNode(new EntitlementGrantTalentNode(CoreEntitlements.ABILITY_POOL_SIZE,
                nodeDisplay(context, CoreTalentDisplayNodes.POOL_COUNT),
                UUID.fromString("108549d0-7935-4386-bf38-2ca48329305e")));
        line.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        line.addNode(attrNode(context, MAX_HEALTH, 3, 1.0));
        line.addNode(new EntitlementGrantTalentNode(CoreEntitlements.PASSIVE_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.PASSIVE_ABILITY_SLOT),
                UUID.fromString("95725b31-da3a-4a3e-b6cc-e5036a6e9a87")));
        line.addNode(new AbilityGrantTalentNode(MKUAbilities.LIFE_SIPHON,
                nodeDisplay(context, MKUTalentDisplayNodes.LIFE_SIPHON)));
        tree.addLine(line);

        TalentLineDefinition magic = new TalentLineDefinition(tree, "b");
        magic.addNode(new EntitlementGrantTalentNode(CoreEntitlements.BASIC_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.ABILITY_SLOT),
                UUID.fromString("2e1ff629-b139-4303-831d-1c1bc5ebc21e")));
        magic.addNode(attrNode(context, MAX_MANA, 2, 5.0));
        magic.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        magic.addNode(attrNode(context, MAX_MANA, 3, 1.0));
        magic.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        magic.addNode(new EntitlementGrantTalentNode(CoreEntitlements.ULTIMATE_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.ULTIMATE_ABILITY_SLOT),
                UUID.fromString("0c751a99-a186-439c-83f1-abb55f67b17e")));
        magic.addNode(new EntitlementGrantTalentNode(CoreEntitlements.ABILITY_POOL_SIZE,
                nodeDisplay(context, CoreTalentDisplayNodes.POOL_COUNT),
                UUID.fromString("9b23bee2-d159-4d32-aca8-1d726de0f875")));
        magic.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        magic.addNode(attrNode(context, MANA_REGEN, 2, 0.25));
        magic.addNode(new EntitlementGrantTalentNode(CoreEntitlements.PASSIVE_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.PASSIVE_ABILITY_SLOT),
                UUID.fromString("4818f37e-16c4-4010-ab7a-a664cab4ab97")));
        magic.addNode(attrNode(context, MAX_HEALTH, 3, 1.0));
        magic.addNode(attrNode(context, MAX_HEALTH, 2, 1.0));
        magic.addNode(attrNode(context, COOLDOWN_REDUCTION, 5, 0.01));
        magic.addNode(new EntitlementGrantTalentNode(CoreEntitlements.ABILITY_POOL_SIZE,
                nodeDisplay(context, CoreTalentDisplayNodes.POOL_COUNT),
                UUID.fromString("93de6f66-4d6d-4721-b774-b12ee92be288")));
        magic.addNode(new EntitlementGrantTalentNode(CoreEntitlements.ULTIMATE_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.ULTIMATE_ABILITY_SLOT),
                UUID.fromString("ecfaa441-35c7-46ce-aa67-f8372bc4fd7d")));
        tree.addLine(magic);

        TalentLineDefinition heal = new TalentLineDefinition(tree, "c");
        heal.addNode(new EntitlementGrantTalentNode(CoreEntitlements.BASIC_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.ABILITY_SLOT),
                UUID.fromString("3a31b74d-cf08-451f-a483-8eb9e47ce89b")));
        heal.addNode(attrNode(context, MANA_REGEN, 2, 0.5));
        heal.addNode(attrNode(context, HEAL_BONUS, 2, 2.0));
        heal.addNode(new EntitlementGrantTalentNode(CoreEntitlements.BASIC_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.ABILITY_SLOT),
                UUID.fromString("de5a37a4-b7e5-4565-9217-2d5d8de5d448")));
        heal.addNode(new EntitlementGrantTalentNode(CoreEntitlements.ABILITY_POOL_SIZE,
                nodeDisplay(context, CoreTalentDisplayNodes.POOL_COUNT),
                UUID.fromString("fbbab80a-c3f8-460f-81cf-5184a7c7f39a")));
        heal.addNode(attrNode(context, MAX_MANA, 3, 1.0));
        heal.addNode(attrNode(context, HEAL_BONUS, 1, 1.0));
        heal.addNode(new EntitlementGrantTalentNode(CoreEntitlements.PASSIVE_ABILITY_SLOT,
                nodeDisplay(context, CoreTalentDisplayNodes.PASSIVE_ABILITY_SLOT),
                UUID.fromString("05865420-0069-45e1-856e-331c9900f99c")));
        heal.addNode(new AbilityGrantTalentNode(MKUAbilities.SOUL_DRAIN,
                nodeDisplay(context, MKUTalentDisplayNodes.SOUL_DRAIN)));
        tree.addLine(heal);
        return tree;
    }

    public static void bootstrap(BootstrapContext<TalentTreeDefinition> context) {
        context.register(WARRIOR, makeWarriorTree(context));
        context.register(PRIEST, makePriestTree(context));
        context.register(MAGE, makeMageTree(context));
        context.register(KNIGHT, makeKnightTree(context));
        context.register(GREEN_KNIGHT, makeGreenKnightTree(context));
        context.register(KNAVE, makeKnaveTree(context));
        context.register(CORE, makeCoreTree(context));
    }
}
