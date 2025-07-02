package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.talents.TalentLineDefinition;
import com.chaosbuffalo.mkcore.core.talents.TalentNode;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AttributeTalent;
import com.chaosbuffalo.mkcore.data.providers.TalentTreeProvider;
import com.chaosbuffalo.mkcore.init.CoreTalents;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUTalents;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.logging.log4j.core.Core;
import org.checkerframework.checker.units.qual.A;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MKUTalentTreeProvider extends TalentTreeProvider {

    public MKUTalentTreeProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> registries) {
        super(generator, registries, MKUltra.MODID);
    }


    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return CompletableFuture.allOf(
                generateCoreTree(pOutput),
                generateGreenKnightTree(pOutput),
                generateKnaveTree(pOutput),
                generateKnightTalentTree(pOutput),
                generateMageTalentTree(pOutput),
                generatePriestTalentTree(pOutput),
                generateWarriorTalentTree(pOutput)
        );
    }

    private CompletableFuture<?> generateWarriorTalentTree(CachedOutput pOutput) {
        TalentTreeDefinition tree = new TalentTreeDefinition(MKUltra.id("warrior"));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        tree.addLine(aLine);
        aLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 2, 0.1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.ARMOR_TALENT, 1, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MOVEMENT_SPEED_TALENT, 2, 0.01));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 2, 0.1));
        // FIXME: Dual wield was here
        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        tree.addLine(bLine);
        bLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 2, 0.1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_TALENT, 3, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 2, 0.1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_TALENT, 2, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MOVEMENT_SPEED_TALENT, 3, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.HEALTH_REGEN_TALENT, 2, 0.25));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 3, 0.1));
        // FIXME: bolstering roar was here
        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        tree.addLine(cLine);
        cLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 2, 0.1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.HEALTH_REGEN_TALENT, 2, 0.25));
        cLine.addNode(new AttributeTalentNode(CoreTalents.ARMOR_TALENT, 1, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 2, 0.1));
        // FIXME: blademaster went here
        return writeDefinition(tree, pOutput);
    }

    private CompletableFuture<?> generatePriestTalentTree(CachedOutput pOutput) {
        TalentTreeDefinition tree = new TalentTreeDefinition(MKUltra.id("priest"));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        tree.addLine(aLine);
        aLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 1, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MOVEMENT_SPEED_TALENT, 3, 0.01));
        aLine.addNode(new AttributeTalentNode(CoreTalents.COOLDOWN_REDUCTION_TALENT, 2, 0.025));
        aLine.addNode(new AttributeTalentNode(CoreTalents.HOLY_DAMAGE_TALENT, 5, 1));
        // FIXME: Armor Training here
        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        tree.addLine(bLine);
        bLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.COOLDOWN_REDUCTION_TALENT, 2, 0.025));
        bLine.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 1, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 4, 0.25));
        bLine.addNode(new AttributeTalentNode(CoreTalents.COOLDOWN_REDUCTION_TALENT, 2, 0.025));
        bLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_TALENT, 2, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.COOLDOWN_REDUCTION_TALENT, 2, 0.025));
        // FIXME: Healing Rain
        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        tree.addLine(cLine);
        cLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, .25));
        cLine.addNode(new AttributeTalentNode(CoreTalents.NATURE_DAMAGE_TALENT, 5, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 4, 0.25));
        cLine.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 1, 1));
        // FIXME: Guardian Angle
        return writeDefinition(tree, pOutput);
    }

    private CompletableFuture<?> generateMageTalentTree(CachedOutput pOutput) {
        TalentTreeDefinition tree = new TalentTreeDefinition(MKUltra.id("mage"));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        tree.addLine(aLine);
        aLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_TALENT, 2, 0.01));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        aLine.addNode(new AttributeTalentNode(CoreTalents.FIRE_DAMAGE_TALENT, 5, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.FIRE_RESISTANCE_TALENT, 5, 0.01));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MOVEMENT_SPEED_TALENT, 2, 0.01));
        aLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_MULTIPLIER_TALENT, 2, 0.2));
        // FIXME: Burning Soul here
        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        tree.addLine(bLine);
        bLine.addNode(new AttributeTalentNode(CoreTalents.ARCANE_DAMAGE_TALENT, 5, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        bLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_MULTIPLIER_TALENT, 2, 0.2));
        bLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_TALENT, 2, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_MULTIPLIER_TALENT, 2, 0.2));
        bLine.addNode(new AttributeTalentNode(CoreTalents.ARCANE_RESISTANCE_TALENT, 5, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_MULTIPLIER_TALENT, 2, 0.2));
        // FIXME: Meteor here
        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        tree.addLine(cLine);
        cLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 4, 0.25));
        cLine.addNode(new AttributeTalentNode(CoreTalents.FROST_DAMAGE_TALENT, 5, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.FROST_RESISTANCE_TALENT, 5, 0.01));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_TALENT, 2, 0.01));
        cLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_MULTIPLIER_TALENT, 2, 0.2));
        // FIXME: Soul drain here
        return writeDefinition(tree, pOutput);
    }

    private CompletableFuture<?> generateKnightTalentTree(CachedOutput pOutput) {
        TalentTreeDefinition tree = new TalentTreeDefinition(MKUltra.id("knight"));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        tree.addLine(aLine);
        aLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.ARMOR_TALENT, 2, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MAX_POISE_TALENT, 2, 2.0));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.HEALTH_REGEN_TALENT, 2, 0.25));
        // FIXME: 2 handed style
        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        tree.addLine(bLine);
        bLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.HEALTH_REGEN_TALENT, 4, 0.25));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 4, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 3, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        bLine.addNode(new AttributeTalentNode(CoreTalents.ARCANE_RESISTANCE_TALENT, 3, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.ARMOR_TALENT, 4, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        // FIXME: Righteous judgement
        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        tree.addLine(cLine);
        cLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.ARCANE_RESISTANCE_TALENT, 2, 0.01));
        cLine.addNode(new AttributeTalentNode(CoreTalents.HEALTH_REGEN_TALENT, 2, 0.25));
        cLine.addNode(new AttributeTalentNode(CoreTalents.ARMOR_TALENT, 2, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        // FIXME: holy aura
        return writeDefinition(tree, pOutput);
    }

    private CompletableFuture<?> generateGreenKnightTree(CachedOutput pOutput) {
        TalentTreeDefinition tree = new TalentTreeDefinition(MKUltra.id("green_knight"));
        tree.setVersion(1);
        TalentLineDefinition line = new TalentLineDefinition(tree, "a");
        line.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 3, 4.0));
        line.addNode(new AttributeTalentNode(CoreTalents.MAX_POISE_TALENT, 2, 2.0));
        line.addNode(new AttributeTalentNode(CoreTalents.POISE_REGEN_TALENT, 2, 0.25));
        line.addNode(new AttributeTalentNode(CoreTalents.POISE_BREAK_CD_TALENT, 2, -0.5));
        line.addNode(new AttributeTalentNode(CoreTalents.POISE_REGEN_TALENT, 2, 0.25));
        TalentLineDefinition soul = new TalentLineDefinition(tree, "b");
        tree.addLine(line);
        soul.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 3, 4.0));
        soul.addNode(new EntitlementGrantTalentNode(CoreTalents.POOL_COUNT_TALENT, UUID.fromString("40525592-c013-46f7-84da-1543d5a28cfc")));
        soul.addNode(new AttributeTalentNode(CoreTalents.MAX_POISE_TALENT, 3, 2.0));
        soul.addNode(new AttributeTalentNode(CoreTalents.NATURE_DAMAGE_TALENT, 5, 1));
        soul.addNode(new AttributeTalentNode(CoreTalents.HEAL_EFFICIENCY_TALENT, 5, 0.02));
        soul.addNode(new AttributeTalentNode(CoreTalents.ARMOR_TALENT, 5, 1.0));
        soul.addNode(new TalentNode(MKUTalents.GREEN_SOUL_TALENT.get()));
        tree.addLine(soul);
        TalentLineDefinition healing = new TalentLineDefinition(tree, "c");
        healing.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 3, 4.0));
        healing.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 2, 1.0));
        healing.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 3, 2.0));
        healing.addNode(new AttributeTalentNode(CoreTalents.MAX_POISE_TALENT, 2, 2.0));
        healing.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 1, 0.5));
        tree.addLine(healing);
        return writeDefinition(tree, pOutput);
    }

    private CompletableFuture<?> generateKnaveTree(CachedOutput pOutput) {
        TalentTreeDefinition tree = new TalentTreeDefinition(MKUltra.id("knave"));
        tree.setVersion(1);
        tree.setDefault(true);
        TalentLineDefinition aLine = new TalentLineDefinition(tree, "a");
        tree.addLine(aLine);
        aLine.addNode(new AttributeTalentNode(CoreTalents.BLEED_DAMAGE_TALENT, 1, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.BLEED_RESISTANCE_TALENT, 3, 0.01));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_TALENT, 2, 0.01));
        aLine.addNode(new AttributeTalentNode(CoreTalents.BLEED_DAMAGE_TALENT, 1, 1));
        aLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        // FIXME: life siphon used to be here
        TalentLineDefinition bLine = new TalentLineDefinition(tree, "b");
        tree.addLine(bLine);
        bLine.addNode(new AttributeTalentNode(CoreTalents.ATTACK_DAMAGE_TALENT, 1, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MOVEMENT_SPEED_TALENT, 3, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.ATTACK_DAMAGE_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MOVEMENT_SPEED_TALENT, 4, 0.01));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.ATTACK_DAMAGE_TALENT, 3, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.ARMOR_TALENT, 1, 1));
        bLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_MULTIPLIER_TALENT, 5, 0.1));
        // FIXME: backstab
        TalentLineDefinition cLine = new TalentLineDefinition(tree, "c");
        tree.addLine(cLine);
        cLine.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.SPELL_CRIT_TALENT, 2, 0.01));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MOVEMENT_SPEED_TALENT, 3, 0.01));
        cLine.addNode(new AttributeTalentNode(CoreTalents.BLEED_DAMAGE_TALENT, 1, 1));
        cLine.addNode(new AttributeTalentNode(CoreTalents.BLEED_RESISTANCE_TALENT, 2, 0.01));
        cLine.addNode(new AttributeTalentNode(CoreTalents.MELEE_CRIT_TALENT, 3, 0.01));
        // FIXME: extended duration
        return writeDefinition(tree, pOutput);
    }


    private CompletableFuture<?> generateCoreTree(CachedOutput pOutput) {
        TalentTreeDefinition tree = new TalentTreeDefinition(MKUltra.id("core"));
        tree.setVersion(2);
        tree.setDefault(true);
        TalentLineDefinition line = new TalentLineDefinition(tree, "a");
        line.addNode(new EntitlementGrantTalentNode(CoreTalents.ABILITY_SLOT_TALENT, UUID.fromString("119917ea-b852-4cb5-8bfe-2cdad488f279")));
        line.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 5.0));
        line.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 1.0));
        line.addNode(new EntitlementGrantTalentNode(CoreTalents.ABILITY_SLOT_TALENT, UUID.fromString("121817fa-1cfc-4334-aa77-13c02ede83ff")));
        line.addNode(new EntitlementGrantTalentNode(CoreTalents.POOL_COUNT_TALENT, UUID.fromString("108549d0-7935-4386-bf38-2ca48329305e")));
        line.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        line.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 3, 1.0));
        line.addNode(new EntitlementGrantTalentNode(CoreTalents.PASSIVE_ABILITY_SLOT_TALENT, UUID.fromString("95725b31-da3a-4a3e-b6cc-e5036a6e9a87")));
        line.addNode(new TalentNode(MKUTalents.LIFE_SIPHON_TALENT.get()));
        tree.addLine(line);
        TalentLineDefinition magic = new TalentLineDefinition(tree, "b");
        magic.addNode(new EntitlementGrantTalentNode(CoreTalents.ABILITY_SLOT_TALENT, UUID.fromString("2e1ff629-b139-4303-831d-1c1bc5ebc21e")));
        magic.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 2, 5.0));
        magic.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        magic.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 3, 1.0));
        magic.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        magic.addNode(new EntitlementGrantTalentNode(CoreTalents.ULTIMATE_ABILITY_SLOT_TALENT, UUID.fromString("0c751a99-a186-439c-83f1-abb55f67b17e")));
        magic.addNode(new EntitlementGrantTalentNode(CoreTalents.POOL_COUNT_TALENT, UUID.fromString("9b23bee2-d159-4d32-aca8-1d726de0f875")));
        magic.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        magic.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.25));
        magic.addNode(new EntitlementGrantTalentNode(CoreTalents.PASSIVE_ABILITY_SLOT_TALENT, UUID.fromString("4818f37e-16c4-4010-ab7a-a664cab4ab97")));
        magic.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 3, 1.0));
        magic.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 2, 1.0));
        magic.addNode(new AttributeTalentNode(CoreTalents.COOLDOWN_REDUCTION_TALENT, 5, 0.01));
        magic.addNode(new EntitlementGrantTalentNode(CoreTalents.POOL_COUNT_TALENT, UUID.fromString("93de6f66-4d6d-4721-b774-b12ee92be288")));
        magic.addNode(new EntitlementGrantTalentNode(CoreTalents.ULTIMATE_ABILITY_SLOT_TALENT, UUID.fromString("ecfaa441-35c7-46ce-aa67-f8372bc4fd7d")));
        tree.addLine(magic);
        TalentLineDefinition heal = new TalentLineDefinition(tree, "c");
        heal.addNode(new EntitlementGrantTalentNode(CoreTalents.ABILITY_SLOT_TALENT, UUID.fromString("3a31b74d-cf08-451f-a483-8eb9e47ce89b")));
        heal.addNode(new AttributeTalentNode(CoreTalents.MANA_REGEN_TALENT, 2, 0.5));
        heal.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 2, 2.0));
        heal.addNode(new EntitlementGrantTalentNode(CoreTalents.ABILITY_SLOT_TALENT, UUID.fromString("de5a37a4-b7e5-4565-9217-2d5d8de5d448")));
        heal.addNode(new EntitlementGrantTalentNode(CoreTalents.POOL_COUNT_TALENT, UUID.fromString("fbbab80a-c3f8-460f-81cf-5184a7c7f39a")));
        heal.addNode(new AttributeTalentNode(CoreTalents.MAX_MANA_TALENT, 3, 1.0));
        heal.addNode(new AttributeTalentNode(CoreTalents.HEAL_BONUS_TALENT, 1, 1.0));
        heal.addNode(new EntitlementGrantTalentNode(CoreTalents.PASSIVE_ABILITY_SLOT_TALENT, UUID.fromString("05865420-0069-45e1-856e-331c9900f99c")));
        heal.addNode(new TalentNode(MKUTalents.SOUL_DRAIN_TALENT.get()));
        tree.addLine(heal);
        return writeDefinition(tree, pOutput);
    }
}
