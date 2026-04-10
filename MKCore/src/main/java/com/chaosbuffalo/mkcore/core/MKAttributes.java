package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.attributes.AttributeSyncType;
import com.chaosbuffalo.mkcore.attributes.MKPercentageAttribute;
import com.chaosbuffalo.mkcore.attributes.MKRangedAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class MKAttributes {

    public static final DeferredRegister<Attribute> REGISTRY =
            DeferredRegister.create(Registries.ATTRIBUTE, MKCore.MOD_ID);

    public static final Holder<Attribute> MAX_MANA = REGISTRY.register("max_mana", () ->
            new MKRangedAttribute("attribute.name.mk.max_mana", 0, 0, 10000)
            .setSyncType(AttributeSyncType.Public));

    public static final Holder<Attribute> MANA_REGEN = REGISTRY.register("mana_regen", () ->
            new MKRangedAttribute("attribute.name.mk.mana_regen", 0, 0, 1024)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> MELEE_CRIT = REGISTRY.register("melee_crit_chance", () ->
            new MKPercentageAttribute("attribute.name.mk.melee_crit_chance", 0.00, 0.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> MELEE_CRIT_MULTIPLIER = REGISTRY.register("melee_crit_multiplier", () ->
            new MKRangedAttribute("attribute.name.mk.melee_crit_multiplier", 1.0, 0.0, 10.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> MULTI_ATTACK_CHANCE = REGISTRY.register("multi_attack_chance", () ->
            new MKRangedAttribute("attribute.name.mk.multi_attack_chance", 0.0, 0.0, 4.0)
                    .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> SPELL_CRIT = REGISTRY.register("spell_crit_chance", () ->
            new MKPercentageAttribute("attribute.name.mk.spell_crit_chance", 0.1, 0.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> SPELL_CRIT_MULTIPLIER = REGISTRY.register("spell_crit_multiplier", () ->
            new MKRangedAttribute("attribute.name.mk.spell_crit_multiplier", 1.5, 0.0, 10.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> RANGED_CRIT = REGISTRY.register("ranged_crit", () ->
            new MKPercentageAttribute("attribute.name.mk.ranged_crit_chance", 0.00, 0.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> RANGED_CRIT_MULTIPLIER = REGISTRY.register("ranged_crit_multiplier", () ->
            new MKRangedAttribute("attribute.name.mk.ranged_crit_multiplier", 1.0, 0.0, 10.0)
            .setSyncType(AttributeSyncType.Private));


    // Everyone Attributes
    public static final Holder<Attribute> RANGED_DAMAGE = REGISTRY.register("ranged_damage", () ->
            new MKRangedAttribute("attribute.name.mk.ranged_damage", 0.0, 0.0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> RANGED_RESISTANCE = REGISTRY.register("ranaged_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.ranged_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    // This is slightly confusing.
    // 1.9 max means the cooldown will progress at most 10x faster than the normal rate. This translates into a 90% reduction in observed cooldown.
    // -3.0 minimum means that a cooldown can be increased up to 5x of the normal value. This translates into a 500% increase in the observed cooldown
    public static final Holder<Attribute> COOLDOWN = REGISTRY.register("cooldown_rate", () ->
            new MKPercentageAttribute("attribute.name.mk.cooldown_rate", 1.0, -3.0, 1.9)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> HEAL_BONUS = REGISTRY.register("heal_bonus", () ->
            new MKRangedAttribute("attribute.name.mk.heal_bonus", 0.0, 0.0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> CASTING_SPEED = REGISTRY.register("casting_speed", () ->
            new MKPercentageAttribute("attribute.name.mk.casting_speed", 1.0, -3.0, 1.9)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> BUFF_DURATION = REGISTRY.register("buff_duration", () ->
            new MKRangedAttribute("attribute.name.mk.buff_duration", 1.0, 0.0, 5.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ARCANE_RESISTANCE = REGISTRY.register("arcane_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.arcane_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ARCANE_DAMAGE = REGISTRY.register("arcane_damage",
            () -> new MKRangedAttribute("attribute.name.mk.arcane_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> FIRE_RESISTANCE = REGISTRY.register("fire_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.fire_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> FIRE_DAMAGE = REGISTRY.register("fire_damage", () ->
            new MKRangedAttribute("attribute.name.mk.fire_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> FROST_RESISTANCE = REGISTRY.register("frost_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.frost_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> FROST_DAMAGE = REGISTRY.register("frost_damage", () ->
            new MKRangedAttribute("attribute.name.mk.frost_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> SHADOW_RESISTANCE = REGISTRY.register("shadow_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.shadow_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> SHADOW_DAMAGE = REGISTRY.register("shadow_damage", () ->
            new MKRangedAttribute("attribute.name.mk.shadow_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> HOLY_RESISTANCE = REGISTRY.register("holy_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.holy_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> HOLY_DAMAGE = REGISTRY.register("holy_damage", () ->
            new MKRangedAttribute("attribute.name.mk.holy_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> NATURE_RESISTANCE = REGISTRY.register("nature_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.nature_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> NATURE_DAMAGE = REGISTRY.register("nature_damage", () ->
            new MKRangedAttribute("attribute.name.mk.nature_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> POISON_RESISTANCE = REGISTRY.register("poison_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.poison_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> POISON_DAMAGE = REGISTRY.register("poison_damage", () ->
            new MKRangedAttribute("attribute.name.mk.poison_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> BLEED_RESISTANCE = REGISTRY.register("bleed_resistance", () ->
            new MKPercentageAttribute("attribute.name.mk.bleed_resistance", 0, -1.0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> BLEED_DAMAGE = REGISTRY.register("bleed_damage", () ->
            new MKRangedAttribute("attribute.name.mk.bleed_damage", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ABJURATION = REGISTRY.register("abjuration", () ->
            new MKRangedAttribute("attribute.name.mk.abjuration", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ALTERATON = REGISTRY.register("alteration", () ->
            new MKRangedAttribute("attribute.name.mk.alteration", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> CONJURATION = REGISTRY.register("conjuration", () ->
            new MKRangedAttribute("attribute.name.mk.conjuration", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> DIVINATION = REGISTRY.register("divination",
            () -> new MKRangedAttribute("attribute.name.mk.divination", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ENCHANTMENT = REGISTRY.register("enchantment", () ->
            new MKRangedAttribute("attribute.name.mk.enchantment", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> PHANTASM = REGISTRY.register("phantasm", () ->
            new MKRangedAttribute("attribute.name.mk.phantasm", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> NECROMANCY = REGISTRY.register("necromancy", () ->
            new MKRangedAttribute("attribute.name.mk.necromancy", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> RESTORATION = REGISTRY.register("restoration", () ->
            new MKRangedAttribute("attribute.name.mk.restoration", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ARETE = REGISTRY.register("arete", () ->
            new MKRangedAttribute("attribute.name.mk.arete", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> PNEUMA = REGISTRY.register("pneuma", () ->
            new MKRangedAttribute("attribute.name.mk.pneuma", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> PANKRATION = REGISTRY.register("pankration", () ->
            new MKRangedAttribute("attribute.name.mk.pankration", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> EVOCATION = REGISTRY.register("evocation", () ->
            new MKRangedAttribute("attribute.name.mk.evocation", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> MARKSMANSHIP = REGISTRY.register("marksmanship", () ->
            new MKRangedAttribute("attribute.name.mk.marksmanship", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> TWO_HAND_SLASH = REGISTRY.register("two_hand_slash", () ->
            new MKRangedAttribute("attribute.name.mk.two_hand_slash", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ONE_HAND_SLASH = REGISTRY.register("one_hand_slash", () ->
            new MKRangedAttribute("attribute.name.mk.one_hand_slash", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> TWO_HAND_BLUNT = REGISTRY.register("two_hand_blunt", () ->
            new MKRangedAttribute("attribute.name.mk.two_hand_blunt", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ONE_HAND_BLUNT = REGISTRY.register("one_hand_blunt", () ->
            new MKRangedAttribute("attribute.name.mk.one_hand_blunt", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> TWO_HAND_PIERCE = REGISTRY.register("two_hand_pierce", () ->
            new MKRangedAttribute("attribute.name.mk.two_hand_pierce", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> ONE_HAND_PIERCE = REGISTRY.register("one_hand_pierce", () ->
            new MKRangedAttribute("attribute.name.mk.one_hand_pierce", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> HAND_TO_HAND = REGISTRY.register("hand_to_hand", () ->
            new MKRangedAttribute("attribute.name.mk.hand_to_hand", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> BLOCK = REGISTRY.register("block", () ->
            new MKRangedAttribute("attribute.name.mk.block", 0, 0, GameConstants.SKILL_MAX)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> MAX_POISE = REGISTRY.register("max_poise", () ->
            new MKRangedAttribute("attribute.name.mk.max_poise", 0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> POISE_REGEN = REGISTRY.register("poise_regen", () ->
            new MKRangedAttribute("attribute.name.mk.poise_regen", 2, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> POISE_BREAK_CD = REGISTRY.register("poise_break_cd", () ->
            new MKRangedAttribute("attribute.name.mk.poise_break_cd", 20.0, 0, 2048)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> BLOCK_EFFICIENCY = REGISTRY.register("block_efficiency", () ->
            new MKPercentageAttribute("attribute.name.mk.block_efficiency", 0, 0, 1.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> HEAL_EFFICIENCY = REGISTRY.register("heal_efficiency", () ->
            new MKPercentageAttribute("attribute.name.mk.heal_efficiency", 1.0, 0, 1000.0)
            .setSyncType(AttributeSyncType.Private));

    public static final Holder<Attribute> HEALTH_REGEN = REGISTRY.register("health_regen", () ->
            new MKRangedAttribute("attribute.name.mk.health_regen", 0, 0, 1024)
                    .setSyncType(AttributeSyncType.Private));

    public static double getValueSafe(Holder<Attribute> attr, LivingEntity target) {
        AttributeInstance instance = target.getAttribute(attr);
        if (instance != null) {
            return instance.getValue();
        } else {
            return attr.value().getDefaultValue();
        }
    }

    public static void iterateEntityAttributes(Consumer<Holder<Attribute>> consumer) {
        consumer.accept(COOLDOWN);
        consumer.accept(CASTING_SPEED);
        consumer.accept(HEAL_BONUS);
        consumer.accept(BUFF_DURATION);
        consumer.accept(ARCANE_DAMAGE);
        consumer.accept(ARCANE_RESISTANCE);
        consumer.accept(FIRE_DAMAGE);
        consumer.accept(FIRE_RESISTANCE);
        consumer.accept(FROST_DAMAGE);
        consumer.accept(FROST_RESISTANCE);
        consumer.accept(SHADOW_DAMAGE);
        consumer.accept(SHADOW_RESISTANCE);
        consumer.accept(HOLY_DAMAGE);
        consumer.accept(HOLY_RESISTANCE);
        consumer.accept(NATURE_DAMAGE);
        consumer.accept(NATURE_RESISTANCE);
        consumer.accept(POISON_DAMAGE);
        consumer.accept(POISON_RESISTANCE);
        consumer.accept(BLEED_DAMAGE);
        consumer.accept(BLEED_RESISTANCE);
        consumer.accept(RANGED_DAMAGE);
        consumer.accept(RANGED_RESISTANCE);
        consumer.accept(ABJURATION);
        consumer.accept(ALTERATON);
        consumer.accept(CONJURATION);
        consumer.accept(DIVINATION);
        consumer.accept(ENCHANTMENT);
        consumer.accept(PHANTASM);
        consumer.accept(NECROMANCY);
        consumer.accept(RESTORATION);
        consumer.accept(ARETE);
        consumer.accept(PNEUMA);
        consumer.accept(PANKRATION);
        consumer.accept(EVOCATION);
        consumer.accept(MARKSMANSHIP);
        consumer.accept(TWO_HAND_BLUNT);
        consumer.accept(ONE_HAND_BLUNT);
        consumer.accept(TWO_HAND_PIERCE);
        consumer.accept(ONE_HAND_PIERCE);
        consumer.accept(TWO_HAND_SLASH);
        consumer.accept(ONE_HAND_SLASH);
        consumer.accept(BLOCK);
        consumer.accept(MAX_POISE);
        consumer.accept(POISE_REGEN);
        consumer.accept(POISE_BREAK_CD);
        consumer.accept(BLOCK_EFFICIENCY);
        consumer.accept(HEAL_EFFICIENCY);
        consumer.accept(MAX_MANA);
        consumer.accept(MANA_REGEN);
        consumer.accept(HAND_TO_HAND);
        consumer.accept(HEALTH_REGEN);
        consumer.accept(MULTI_ATTACK_CHANCE);
    }

    public static void iteratePlayerAttributes(Consumer<Holder<Attribute>> consumer) {
        consumer.accept(MELEE_CRIT);
        consumer.accept(MELEE_CRIT_MULTIPLIER);
        consumer.accept(SPELL_CRIT);
        consumer.accept(SPELL_CRIT_MULTIPLIER);
        consumer.accept(RANGED_CRIT);
        consumer.accept(RANGED_CRIT_MULTIPLIER);
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
